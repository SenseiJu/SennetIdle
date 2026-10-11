package me.senseiju.sennetidle.utils

import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sennetidle.utils.extensions.runTask
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer
import java.util.function.Function

val EMPTY_COMPONENT = "".component()
const val GREEN_TICK_COMPONENT = "<green><b>[✔]"
const val RED_CROSS_COMPONENT = "<red><b>[❌]"

fun tickComponent(boolean: Boolean): String {
    return if (boolean) GREEN_TICK_COMPONENT else RED_CROSS_COMPONENT
}

val FILLER_ITEM: ItemProvider = ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)

/**
 * Creates a gui item whose [provider] is re-evaluated every [updatePeriod] ticks (if above 0) and after every click
 */
fun guiItem(updatePeriod: Int = 0, provider: () -> ItemProvider, onClick: (Click) -> Unit = {}): Item {
    return Item.builder()
        .setItemProvider(Function<Player, ItemProvider> { provider() })
        .addClickHandler(Consumer<Click> { onClick(it) })
        .updateOnClick()
        .apply { if (updatePeriod > 0) updatePeriodically(updatePeriod) }
        .build()
}

fun guiItem(provider: ItemProvider, onClick: (Click) -> Unit = {}): Item {
    return Item.builder()
        .setItemProvider(provider)
        .addClickHandler(Consumer<Click> { onClick(it) })
        .build()
}

/**
 * Opens [gui] in a window, [onPlayerClose] is run on the next tick if the player closes it themselves
 */
fun Player.openGui(title: Component, gui: Gui, onPlayerClose: (() -> Unit)? = null) {
    Window.builder()
        .setUpperGui(gui)
        .setTitle(title)
        .apply {
            if (onPlayerClose != null) {
                addCloseHandler { reason ->
                    if (reason == InventoryCloseEvent.Reason.PLAYER) {
                        runTask(onPlayerClose)
                    }
                }
            }
        }
        .open(this)
}
