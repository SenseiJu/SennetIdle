package gg.idlemob.idlemobcore.prestige

import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.economy.EconomyService
import gg.idlemob.idlemobcore.economy.InsufficientFundsException
import gg.idlemob.idlemobcore.framework.database.Database
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.stats.StatContext
import gg.idlemob.idlemobcore.stats.StatModifiers
import gg.idlemob.idlemobcore.stats.StatProvider
import gg.idlemob.idlemobcore.user.User
import gg.idlemob.idlemobcore.user.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PrestigeService(
    private val plugin: IdleMobCore,
    private val levels: PrestigeRegistry,
    private val upgrades: UpgradeRegistry,
    private val database: Database,
    private val repository: UserRepository,
    private val economy: EconomyService,
) {
    private val pending = ConcurrentHashMap.newKeySet<UUID>()
    private val notified = ConcurrentHashMap<UUID, Int>()

    private val messages
        get() = plugin.messages.prestige

    fun nextLevel(user: User): PrestigeLevelConfig? = levels.level(user.prestige + 1)

    fun remaining(user: User, level: PrestigeLevelConfig, resource: String): Double {
        val required = level.resources[resource] ?: return 0.0
        return (required - user.getPrestigeProgress(resource)).coerceAtLeast(0.0)
    }

    fun resourcesDone(user: User, level: PrestigeLevelConfig): Boolean = level.resources.keys.all { remaining(user, level, it) <= 0 }

    fun canAfford(user: User, level: PrestigeLevelConfig): Boolean = level.costs().all { (currency, amount) -> user.getBalance(currency) >= amount }

    fun isReady(user: User, level: PrestigeLevelConfig): Boolean =
        user.stage >= level.requiredStage && resourcesDone(user, level) && canAfford(user, level)

    fun commit(player: Player, user: User, resource: String) {
        val level = nextLevel(user) ?: return
        if (user.uuid in pending) {
            return
        }

        val amount = minOf(user.getResource(resource), remaining(user, level, resource))
        val name = plugin.resources[resource]?.display?.name ?: resource
        if (amount <= 0) {
            player.sendMessage(messages.nothingToCommit(name))
            return
        }

        synchronized(user) {
            user.addResource(resource, -amount)
            user.addPrestigeProgress(resource, amount)
        }

        player.sendMessage(messages.committed(amount, name))
        EventLogger.debug("prestige_resource_committed", player) {
            "resource" to resource
            "amount" to amount
            "prestige" to user.prestige + 1
        }
    }

    fun prestige(player: Player, user: User) {
        val level = nextLevel(user)
        if (level == null) {
            player.sendMessage(messages.maxPrestige())
            return
        }

        if (user.stage < level.requiredStage || !resourcesDone(user, level)) {
            player.sendMessage(messages.notReady())
            return
        }

        level.costs().entries.firstOrNull { (currency, amount) -> user.getBalance(currency) < amount }?.let { (currency, _) ->
            player.sendMessage(plugin.messages.economy.notEnough(currency))
            return
        }

        if (!pending.add(user.uuid)) {
            return
        }

        val next = user.prestige + 1
        plugin.scope.launch {
            try {
                plugin.users.withSaveLock {
                    val changes = database.query {
                        val changes = level.costs().map { (currency, amount) ->
                            economy.changeIn(this, user.uuid, currency, -amount, "prestige:$next")
                        }

                        val snapshot = user.snapshot().copy(
                            stage = 1,
                            wave = 1,
                            prestige = next,
                            prestigePoints = user.prestigePoints + level.points,
                            prestigeProgress = emptyMap(),
                        )
                        with(repository) { write(listOf(snapshot)) }

                        changes
                    }

                    withContext(plugin.scope.main) {
                        synchronized(user) {
                            user.stage = 1
                            user.wave = 1
                            user.prestige = next
                            user.prestigePoints += level.points
                            user.clearPrestigeProgress()
                        }
                        changes.forEach(economy::record)
                        notified.remove(user.uuid)

                        plugin.fight.restart(player)
                        player.sendMessage(messages.prestiged(next, level.points))
                        player.showTitle(Title.title(messages.prestigeTitle(next), messages.prestigeSubtitle(level.points)))
                        EventLogger.info("prestiged", player) {
                            "prestige" to next
                            "points" to level.points
                        }
                    }
                }
            } catch (ex: InsufficientFundsException) {
                withContext(plugin.scope.main) { player.sendMessage(plugin.messages.economy.notEnough(ex.currency)) }
            } catch (ex: Exception) {
                EventLogger.error("prestige_failed", ex, player) { "prestige" to next }
                withContext(plugin.scope.main) { player.sendMessage(messages.failed()) }
            } finally {
                pending.remove(user.uuid)
            }
        }
    }

    fun notifyIfReady(player: Player, user: User) {
        val level = nextLevel(user) ?: return
        if (user.stage < level.requiredStage || notified[user.uuid] == user.prestige) {
            return
        }

        notified[user.uuid] = user.prestige
        player.sendMessage(messages.available())
    }

    fun buyUpgrade(player: Player, user: User, id: String) {
        val upgrade = upgrades[id] ?: return
        val level = user.getUpgrade(id)
        if (level >= upgrade.maxLevel) {
            player.sendMessage(messages.upgradeMaxed())
            return
        }

        val cost = upgrade.costFor(level)
        synchronized(user) {
            if (user.prestigePoints < cost) {
                player.sendMessage(messages.notEnoughPoints(cost))
                return
            }
            user.prestigePoints -= cost
            user.setUpgrade(id, level + 1)
        }

        player.sendMessage(messages.upgradeBought(upgrade.display.name, level + 1))
        EventLogger.info("upgrade_bought", player) {
            "upgrade" to id
            "level" to level + 1
            "cost" to cost
        }
    }
}

class UpgradeStatProvider(private val upgrades: UpgradeRegistry) : StatProvider {

    override fun modify(context: StatContext, modifiers: StatModifiers) {
        context.user.upgrades().forEach { (id, level) ->
            val upgrade = upgrades[id] ?: return@forEach
            val amount = upgrade.perLevel * level.coerceAtMost(upgrade.maxLevel)

            when (upgrade.mode) {
                UpgradeMode.FLAT -> modifiers.add(upgrade.stat, amount)
                UpgradeMode.PERCENT -> modifiers.addPercent(upgrade.stat, amount)
            }
        }
    }
}
