package me.senseiju.sennetidle.utils

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

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
