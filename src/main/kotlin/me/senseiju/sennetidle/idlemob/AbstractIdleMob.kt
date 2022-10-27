package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.upgrades.upgradeData.PlayerUpgrade
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.MultiRunnable
import me.senseiju.sennetidle.utils.extensions.component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import java.util.*
import kotlin.math.roundToLong

abstract class AbstractIdleMob(
    protected val user: User,
    location: Location,
    entityType: EntityType,
    val maxHealth: Long
) {
    val entity = location.world.spawnEntity(location.clone().add(0.0, 10.0, 0.0), entityType) as LivingEntity

    var currentHealth = maxHealth
        protected set
    private val healthBarKey = NamespacedKey(plugin, "idle-mob-health-${getPlayerUUID().toString().lowercase()}")
    private val healthBar = plugin.server.createBossBar(healthBarKey, "Health", BarColor.RED, BarStyle.SOLID)

    protected val runnables = MultiRunnable()

    var cps = 0
        private set

    init {
        healthBar.removeAll()

        entity.setAI(false)
        entity.isSilent = true
        entity.maximumNoDamageTicks = 5
        entity.removeWhenFarAway = false

        runnables.addRepeatingRunnable(1, 1) {
            entity.teleport(location)
        }
        runnables.start(plugin, false)

        Bukkit.getOnlinePlayers()
            .filterNot { it.uniqueId == getPlayerUUID() }
            .forEach {
                @Suppress("UnstableApiUsage") it.hideEntity(plugin, entity)
            }

        user.withPlayer {
            it.setPlayerTime(6000, false)
            @Suppress("UnstableApiUsage") it.showEntity(plugin, entity)

            healthBar.addPlayer(it)
        }

        updateEntityHealthVisuals()
    }

    open fun dispose() {
        entity.remove()

        healthBar.removeAll()
        plugin.server.removeBossBar(healthBarKey)

        runnables.cancel()
    }

    fun getPlayerUUID(): UUID {
        return user.playerId
    }

    fun getEntityUUID(): UUID {
        return entity.uniqueId
    }

    protected fun updateEntityHealthVisuals() {
        healthBar.progress = currentHealth.coerceAtLeast(0).toDouble() / maxHealth
    }

    fun onDamageByPlayer() {
        val dm = DamageModifier(user.dps * 0.2)

        Upgrade.playerUpgrades.forEach { it.dataAs<PlayerUpgrade>().onPlayerDamageIdleMob(user, dm) }

        currentHealth -= dm.finalDamage.roundToLong()

        if (currentHealth <= 0) {
            entity.health = 0.0
        }

        cps++
        runnables.addRepeatingRunnable(20, 1) { cps-- }

        onHit()

        updateEntityHealthVisuals()
    }

    fun onDeath() {
        if (currentHealth <= 0) {
            if (user.canPromote()) {
                user.withPlayer {
                    it.sendMessage("<green>You can promote <white>/promote".component())
                }
            } else {
                user.currentWave++
            }

            onKill(true)
        } else {
            onKill(false)
        }

        dispose()
    }

    abstract fun onHit()
    abstract fun onKill(success: Boolean)
}