package gg.idlemob.idlemobcore.generator

import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.framework.extensions.formatDuration
import gg.idlemob.idlemobcore.framework.text.bar
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.user.User
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.*

private const val ITEM_SCALE = 0.7f
private const val SPIN_STEP = 0.8f

private class SlotDisplay(val slot: Int, val text: TextDisplay, val item: ItemDisplay, val interaction: Interaction) {
    var angle = 0f
    val entities: List<Entity> get() = listOf(text, item, interaction)
    val valid: Boolean get() = entities.all { it.isValid }
}

class GeneratorDisplays(private val plugin: IdleMobCore, private val manager: GeneratorManager) : Listener {
    private val displays = HashMap<UUID, MutableList<SlotDisplay>>()
    private val owners = HashMap<UUID, Pair<UUID, Int>>()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun update(player: Player) {
        val user = plugin.users[player.uniqueId] ?: return
        val list = displays.getOrPut(player.uniqueId) { mutableListOf() }

        manager.slots.forEachIndexed { slot, config ->
            val location = config.location.toLocation()
            var display = list.firstOrNull { it.slot == slot }

            if (display == null || !display.valid) {
                display?.let { remove(it) }
                list.remove(display)
                if (!location.isChunkLoaded) {
                    return@forEachIndexed
                }
                display = spawn(player, slot).also { list += it }
            }

            render(player, user, slot, display)
        }
    }

    fun removeAll() {
        displays.values.flatten().forEach(::remove)
        displays.clear()
        owners.clear()
    }

    private fun spawn(player: Player, slot: Int): SlotDisplay {
        val location = manager.slots[slot].location.toLocation()
        val world = location.world

        val text = world.spawn(location.clone().add(0.0, 2.1, 0.0), TextDisplay::class.java) {
            it.billboard = Display.Billboard.CENTER
            it.alignment = TextDisplay.TextAlignment.CENTER
            it.isShadowed = true
            setup(it)
        }
        val item = world.spawn(location.clone().add(0.0, 1.3, 0.0), ItemDisplay::class.java) {
            it.billboard = Display.Billboard.FIXED
            it.transformation = Transformation(Vector3f(), AxisAngle4f(), Vector3f(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE), AxisAngle4f())
            setup(it)
        }
        val interaction = world.spawn(location, Interaction::class.java) {
            it.interactionWidth = 1.2f
            it.interactionHeight = 2.2f
            it.isResponsive = true
            setup(it)
        }

        val display = SlotDisplay(slot, text, item, interaction)
        display.entities.forEach {
            player.showEntity(plugin, it)
            owners[it.uniqueId] = player.uniqueId to slot
        }
        return display
    }

    private fun setup(entity: Entity) {
        entity.isVisibleByDefault = false
        entity.isPersistent = false
    }

    private fun remove(display: SlotDisplay) {
        display.entities.forEach {
            owners.remove(it.uniqueId)
            it.remove()
        }
    }

    private fun render(player: Player, user: User, slot: Int, display: SlotDisplay) {
        val status = manager.status(player, user, slot)
        val crafting = user.getGenerator(slot)?.let(manager::blueprint)?.takeIf { status == GeneratorStatus.RUNNING || status == GeneratorStatus.PAUSED }

        val line = when (status) {
            GeneratorStatus.LOCKED -> "<red><b>LOCKED"
            GeneratorStatus.EMPTY, GeneratorStatus.MISSING -> "<yellow>Waiting for blueprint"
            GeneratorStatus.PAUSED -> "<red>Paused"
            GeneratorStatus.RUNNING -> {
                val state = user.getGenerator(slot)!!
                val craftMs = crafting!!.craftTime.toMillis()
                "<green>Crafting <item><br>${bar(state.progressMs.toDouble() / craftMs, 20)} <white>${(craftMs - state.progressMs).formatDuration()}"
            }
        }

        display.text.text(mini(
            "<gold><b>Generator <slot></b><br>$line",
            Placeholder.unparsed("slot", (slot + 1).toString()),
            Placeholder.parsed("item", crafting?.outputName ?: ""),
        ))

        display.item.setItemStack(crafting?.let { plugin.resources[it.id] }?.display?.toItemStack() ?: ItemStack.empty())
        spin(display)
    }

    private fun spin(display: SlotDisplay) {
        display.angle = (display.angle + SPIN_STEP) % (2 * Math.PI.toFloat())
        display.item.interpolationDelay = 0
        display.item.interpolationDuration = 20
        display.item.transformation = Transformation(Vector3f(), AxisAngle4f(display.angle, 0f, 1f, 0f), Vector3f(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE), AxisAngle4f())
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        update(event.player)
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        displays.remove(event.player.uniqueId)?.forEach(::remove)
    }

    @EventHandler
    private fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val (owner, slot) = owners[event.rightClicked.uniqueId] ?: return
        event.isCancelled = true
        if (owner != event.player.uniqueId) {
            return
        }

        if (event.hand == EquipmentSlot.HAND) {
            manager.interact(event.player, slot)
        }
    }
}
