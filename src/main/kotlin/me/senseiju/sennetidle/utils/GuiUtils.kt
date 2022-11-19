package me.senseiju.sennetidle.utils

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.GuiAction
import dev.triumphteam.gui.guis.BaseGui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sentils.runnables.newRunnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

val EMPTY_COMPONENT = "".component()
const val GREEN_TICK_COMPONENT = "<green><b>[✔]"
const val RED_CROSS_COMPONENT = "<red><b>[❌]"

fun tickComponent(boolean: Boolean): String {
    return if (boolean) GREEN_TICK_COMPONENT else RED_CROSS_COMPONENT
}

fun BaseGui.openSync(player: HumanEntity) {
    newRunnable { open(player) }.runTask(plugin)
}

val FILLER_ITEM = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
        .name("".component())
        .asGuiItem { e ->
            e.isCancelled = true
        }

private val NEXT = Component.text("Next »", NamedTextColor.YELLOW)
fun PaginatedGui.setNextItem(row: Int, col: Int) {
    setItem(
        row,
        col,
        ItemBuilder.from(Material.PAPER)
            .name(NEXT)
            .asGuiItem { e ->
                e.isCancelled = true
                next()
            }
    )
}

private val PREVIOUS = Component.text("« Previous", NamedTextColor.YELLOW)
fun PaginatedGui.setPreviousItem(row: Int, col: Int) {
    setItem(
        row,
        col,
        ItemBuilder.from(Material.PAPER)
            .name(PREVIOUS)
            .asGuiItem { e ->
                e.isCancelled = true
                previous()
            }
    )
}

class UpdatingGuiItem : GuiItem {
    constructor(material: Material) : super(material)
    constructor(material: Material, action: GuiAction<InventoryClickEvent>) : super(material, action)
    constructor(itemStack: ItemStack) : super(itemStack)
    constructor(itemStack: ItemStack, action: GuiAction<InventoryClickEvent>) : super(itemStack, action)

    var updateAction: (ItemStack) -> Unit = {}
        private set

    fun setUpdateAction(action: (ItemStack) -> Unit): UpdatingGuiItem {
        updateAction = action

        return this
    }
}

fun BaseGui.openUpdating(delay: Long, period: Long, player: HumanEntity, closeGuiAction: (InventoryCloseEvent) -> Unit = {}) {
    openSync(player)

    val updatingGuiItems = guiItems.values.filterIsInstance(UpdatingGuiItem::class.java)
    val runnable = newRunnable {
        updatingGuiItems.forEach {
            it.updateAction(it.itemStack)
        }

        update()
    }

    runnable.runTaskTimerAsynchronously(plugin, delay, period)

    setCloseGuiAction { event ->
        runnable.cancel()

        closeGuiAction(event)
    }
}