package me.senseiju.sennetidle.utils

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.GuiAction
import dev.triumphteam.gui.guis.BaseGui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import kotlinx.serialization.descriptors.PrimitiveKind
import me.senseiju.sentils.runnables.newRunnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.properties.Delegates

fun fillerItem(): GuiItem {
    return ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
        .name(Component.text(" "))
        .asGuiItem { e ->
            e.isCancelled = true
        }
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

    private lateinit var updatingRunnable: BukkitRunnable
    private var updatingDelay by Delegates.notNull<Long>()
    private var updatingPeriod by Delegates.notNull<Long>()

    fun setUpdatingRunnable(delay: Long, period: Long, action: () -> Unit): UpdatingGuiItem {
        updatingRunnable = newRunnable(action)
        updatingDelay = delay
        updatingPeriod = period

        return this
    }

    fun startUpdatingRunnable(plugin: JavaPlugin) {
        if (this::updatingRunnable.isInitialized) {
            updatingRunnable.runTaskTimer(plugin, updatingDelay, updatingPeriod)
        }
    }

    fun cancelUpdatingRunnable() {
        if (this::updatingRunnable.isInitialized && !updatingRunnable.isCancelled) {
            updatingRunnable.cancel()
        }
    }
}

fun BaseGui.openUpdating(plugin: JavaPlugin, humanEntity: HumanEntity, closeGuiAction: (InventoryCloseEvent) -> Unit = {}) {
    open(humanEntity)

    getUpdatingGuiItems().forEach {
        it.startUpdatingRunnable(plugin)
    }

    setCloseGuiAction { event ->
        getUpdatingGuiItems().forEach {
            it.cancelUpdatingRunnable()
        }

        closeGuiAction(event)
    }
}

private fun BaseGui.getUpdatingGuiItems(): List<UpdatingGuiItem> {
    return guiItems.values.filterIsInstance(UpdatingGuiItem::class.java)
}
