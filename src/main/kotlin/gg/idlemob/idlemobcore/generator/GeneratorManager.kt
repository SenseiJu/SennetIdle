package gg.idlemob.idlemobcore.generator

import gg.idlemob.idlemobcore.framework.extensions.giveOrDrop
import gg.idlemob.idlemobcore.GeneratorSlotConfig
import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.blueprint.Blueprint
import gg.idlemob.idlemobcore.blueprint.BlueprintItems
import gg.idlemob.idlemobcore.blueprint.Blueprints
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.resource.ResourceRegistry
import gg.idlemob.idlemobcore.resource.isUnlockedFor
import gg.idlemob.idlemobcore.user.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class GeneratorStatus { LOCKED, EMPTY, RUNNING, PAUSED, MISSING }

class GeneratorManager(
    private val plugin: IdleMobCore,
    private val blueprints: Blueprints,
    private val resources: ResourceRegistry,
    private val repository: GeneratorRepository,
    val items: BlueprintItems,
) {
    private val pending = ConcurrentHashMap.newKeySet<Pair<UUID, Int>>()
    val displays = GeneratorDisplays(plugin, this)

    private val messages
        get() = plugin.messages.generators

    private val ticker = plugin.server.scheduler.runTaskTimer(plugin, Runnable { tick() }, 20, 20)

    val slots: List<GeneratorSlotConfig>
        get() = plugin.cfg.generators.slots

    fun shutdown() {
        ticker.cancel()
        displays.removeAll()
    }

    fun isUnlocked(player: Player, user: User, slot: Int): Boolean {
        val config = slots.getOrNull(slot) ?: return false
        if (config.permission.isNotBlank()) {
            return player.hasPermission(config.permission)
        }
        return user.stage >= config.unlockStage && user.prestige >= config.unlockPrestige || user.prestige > config.unlockPrestige
    }

    fun blueprint(state: GeneratorState): Blueprint? = blueprints[state.blueprint]

    fun status(player: Player, user: User, slot: Int): GeneratorStatus {
        if (!isUnlocked(player, user, slot)) {
            return GeneratorStatus.LOCKED
        }

        val state = user.getGenerator(slot) ?: return GeneratorStatus.EMPTY
        val blueprint = blueprint(state) ?: return GeneratorStatus.MISSING
        val resource = resources[blueprint.id] ?: return GeneratorStatus.MISSING
        return if (resource.isUnlockedFor(user)) GeneratorStatus.RUNNING else GeneratorStatus.PAUSED
    }

    fun canInsert(player: Player, user: User, slot: Int, item: ItemStack?): Boolean {
        val blueprint = items.read(item) ?: return false
        val config = blueprints[blueprint.id] ?: return false
        return config.resource.isUnlockedFor(user) && isUnlocked(player, user, slot) &&
            user.getGenerator(slot) == null && (user.uuid to slot) !in pending
    }

    fun isPending(user: User, slot: Int): Boolean = (user.uuid to slot) in pending

    fun interact(player: Player, slot: Int) {
        val user = plugin.users[player.uniqueId] ?: return

        if (player.isSneaking) {
            if (user.getGenerator(slot) != null) {
                remove(player, user, slot)
            }
            return
        }

        val hand = player.inventory.itemInMainHand
        if (items.read(hand) == null) {
            when {
                !isUnlocked(player, user, slot) -> player.sendMessage(messages.slotLocked())
                user.getGenerator(slot) == null -> player.sendMessage(messages.holdBlueprint())
                else -> player.sendMessage(messages.sneakToRemove())
            }
            return
        }

        when {
            !isUnlocked(player, user, slot) -> player.sendMessage(messages.slotLocked())
            user.getGenerator(slot) != null -> player.sendMessage(messages.slotBusy())
            blueprints[items.read(hand)!!.id]?.resource?.isUnlockedFor(user) == false ->
                player.sendMessage(messages.resourceLocked(blueprints[items.read(hand)!!.id]!!.outputName))
            !canInsert(player, user, slot, hand) -> player.sendMessage(messages.unknownBlueprint(items.read(hand)!!.id))
            else -> {
                val item = hand.clone()
                player.inventory.setItemInMainHand(null)
                insert(player, user, slot, item) {}
            }
        }
    }

    fun insert(player: Player, user: User, slot: Int, item: ItemStack, onFailed: () -> Unit) {
        val blueprint = items.read(item)
        val config = blueprint?.let { blueprints[it.id] }
        if (blueprint == null || config == null || !canInsert(player, user, slot, item) || !pending.add(user.uuid to slot)) {
            player.giveOrDrop(listOf(item))
            onFailed()
            return
        }

        val state = GeneratorState(slot, blueprint.id, blueprint.uuid, blueprint.uses)

        plugin.scope.launch {
            try {
                repository.insert(user.uuid, state)
                withContext(plugin.scope.main) {
                    user.setGenerator(state)
                    player.sendMessage(messages.inserted(config.name, slot + 1))
                    displays.update(player)
                }
                EventLogger.info("blueprint_inserted", player) {
                    "slot" to slot
                    "blueprint" to blueprint.id
                    "blueprint_uuid" to blueprint.uuid
                    "uses" to blueprint.uses
                }
            } catch (ex: DuplicateBlueprintException) {
                EventLogger.warn("blueprint_duplicate_blocked", player) {
                    "blueprint" to blueprint.id
                    "blueprint_uuid" to blueprint.uuid
                }
                withContext(plugin.scope.main) {
                    player.sendMessage(messages.duplicate())
                    onFailed()
                }
            } catch (ex: Exception) {
                EventLogger.error("blueprint_insert_failed", ex, player) { "blueprint_uuid" to blueprint.uuid }
                withContext(plugin.scope.main) {
                    player.giveOrDrop(listOf(item))
                    player.sendMessage(messages.failed())
                    onFailed()
                }
            } finally {
                pending.remove(user.uuid to slot)
            }
        }
    }

    fun remove(player: Player, user: User, slot: Int, onRemoved: () -> Unit = {}) {
        val state = user.getGenerator(slot) ?: return
        if (!pending.add(user.uuid to slot)) {
            return
        }

        plugin.scope.launch {
            try {
                repository.delete(user.uuid, state)
                withContext(plugin.scope.main) {
                    val current = user.removeGenerator(slot) ?: state
                    val name = blueprints[current.blueprint]?.name ?: current.blueprint
                    if (blueprints[current.blueprint] != null && current.uses > 0) {
                        player.giveOrDrop(listOf(items.create(current.blueprint, current.uses, current.blueprintUuid)))
                    }
                    player.sendMessage(messages.removed(name, slot + 1, current.uses))
                    displays.update(player)
                    onRemoved()
                    EventLogger.info("blueprint_removed", player) {
                        "slot" to slot
                        "blueprint" to current.blueprint
                        "blueprint_uuid" to current.blueprintUuid
                        "uses" to current.uses
                    }
                }
            } catch (ex: Exception) {
                EventLogger.error("blueprint_remove_failed", ex, player) { "blueprint_uuid" to state.blueprintUuid }
                withContext(plugin.scope.main) { player.sendMessage(messages.failed()) }
            } finally {
                pending.remove(user.uuid to slot)
            }
        }
    }

    private fun tick() {
        plugin.server.onlinePlayers.forEach { player ->
            val user = plugin.users[player.uniqueId] ?: return@forEach

            user.generators().forEach { state ->
                if (status(player, user, state.slot) == GeneratorStatus.RUNNING && (user.uuid to state.slot) !in pending) {
                    produce(player, user, state)
                }
            }

            displays.update(player)
        }
    }

    private fun produce(player: Player, user: User, state: GeneratorState) {
        val blueprint = blueprint(state) ?: return
        val craftMs = blueprint.craftTime.toMillis()

        val crafted = synchronized(user) {
            state.progressMs += 1000
            var crafted = 0
            while (state.progressMs >= craftMs && state.uses > 0) {
                state.progressMs -= craftMs
                state.uses--
                crafted++
            }
            crafted
        }

        if (crafted > 0) {
            user.addResource(blueprint.id, crafted.toDouble() * blueprint.amount)
            EventLogger.debug("generator_crafted", player) {
                "slot" to state.slot
                "blueprint" to state.blueprint
                "crafts" to crafted
            }
        }

        if (state.uses <= 0) {
            finish(player, user, state, blueprint)
        }
    }

    private fun finish(player: Player, user: User, state: GeneratorState, blueprint: Blueprint) {
        if (!pending.add(user.uuid to state.slot)) {
            return
        }

        user.removeGenerator(state.slot)
        player.sendMessage(messages.usedUp(blueprint.name, state.slot + 1))
        EventLogger.info("blueprint_used_up", player) {
            "slot" to state.slot
            "blueprint" to state.blueprint
            "blueprint_uuid" to state.blueprintUuid
        }

        plugin.scope.launch {
            try {
                repository.delete(user.uuid, state)
            } catch (ex: Exception) {
                EventLogger.error("blueprint_delete_failed", ex, player) { "blueprint_uuid" to state.blueprintUuid }
            } finally {
                pending.remove(user.uuid to state.slot)
            }
        }
    }
}
