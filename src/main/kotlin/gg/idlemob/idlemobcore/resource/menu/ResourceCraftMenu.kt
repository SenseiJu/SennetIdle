package gg.idlemob.idlemobcore.resource.menu

import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import gg.idlemob.idlemobcore.framework.menu.button
import gg.idlemob.idlemobcore.framework.menu.dynamic
import gg.idlemob.idlemobcore.framework.menu.filler
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.plugin
import gg.idlemob.idlemobcore.resource.ResourceConfig
import gg.idlemob.idlemobcore.resource.ResourceCrafter
import gg.idlemob.idlemobcore.resource.ResourceRegistry
import gg.idlemob.idlemobcore.resource.isUnlockedFor
import gg.idlemob.idlemobcore.user.User
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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

class ResourceCraftMenu(
    private val resources: ResourceRegistry,
    private val crafter: ResourceCrafter,
    private val lore: ResourceLore,
    private val onClose: (Player) -> Unit,
) {
    private val messages
        get() = plugin.messages.resources

    fun open(player: Player, user: User, id: String, resource: ResourceConfig) {
        val craftable = resource.components.craftable ?: return

        lateinit var gui: Gui
        val refresh = { gui.notifyWindows() }

        gui = Gui.builder()
            .setStructure(
                "# # # # # # # # #",
                "# . . . . . . . #",
                "# # # # # # # # #",
                "# # # # r # # # #",
                "# # a b c d e # #",
                "# # # # # # # # #",
            )
            .addIngredient('#', filler())
            .addIngredient('r', dynamic {
                ItemBuilder(resource.display.toItemStack()).setName(resource.display.displayName()).setLore(lore.lore(user, id, resource, includeRecipe = false))
            })
            .addIngredient('a', craftButton(player, user, id, resource, refresh) { 1 })
            .addIngredient('b', craftButton(player, user, id, resource, refresh) { 10 })
            .addIngredient('c', craftButton(player, user, id, resource, refresh) { 100 })
            .addIngredient('d', craftButton(player, user, id, resource, refresh) { 1000 })
            .addIngredient('e', craftButton(player, user, id, resource, refresh, all = true) { crafter.maxCrafts(user, id) })
            .build()

        craftable.ingredients.entries.take(7).forEachIndexed { index, (ingredient, needed) ->
            gui.setItem(index + 1, 1, ingredientItem(player, user, ingredient, needed))
        }

        Window.builder()
            .setUpperGui(gui)
            .setTitle(mini("<dark_gray>Craft <name>", Placeholder.parsed("name", resource.display.name)))
            .addCloseHandler(Consumer<InventoryCloseEvent.Reason> { reason ->
                if (reason == InventoryCloseEvent.Reason.PLAYER) {
                    plugin.server.scheduler.runTask(plugin, Runnable { onClose(player) })
                }
            })
            .open(player)
    }

    private fun ingredientItem(player: Player, user: User, id: String, needed: Int): Item {
        val resource = resources[id]

        return Item.builder()
            .setItemProvider(Function<Player, ItemProvider> {
                val have = user.getResource(id)
                val builder = resource?.let { ItemBuilder(it.display.toItemStack()).setName(it.display.displayName()) }
                    ?: button(Material.BARRIER, Component.text(id))

                builder
                    .setLore(listOf(lore.quantity(have), Component.empty(), mini("<gray>Needed per craft: <white><amount>", Placeholder.unparsed("amount", needed.formatSuffixed()))))
                    .setGlint(have >= needed)
            })
            .updatePeriodically(20)
            .addClickHandler(Consumer<Click> {
                if (resource != null && resource.components.craftable != null && resource.isUnlockedFor(user)) {
                    open(player, user, id, resource)
                }
            })
            .build()
    }

    private fun craftButton(
        player: Player,
        user: User,
        id: String,
        resource: ResourceConfig,
        refresh: () -> Unit,
        all: Boolean = false,
        times: () -> Int,
    ): Item {
        return Item.builder()
            .setItemProvider(Function<Player, ItemProvider> {
                val crafts = times()
                val amount = Placeholder.unparsed("amount", crafts.formatSuffixed())
                val name = if (all) mini("<green><b>Craft all <gray>(<amount>)", amount) else mini("<green><b>Craft x<amount>", amount)
                val uses = resource.components.craftable?.ingredients.orEmpty().map { (ingredient, needed) ->
                    lore.ingredient(ingredient, needed.toDouble() * crafts.coerceAtLeast(1), user, available = crafts >= 1)
                }

                val affordable = crafts >= 1 && crafter.maxCrafts(user, id) >= crafts
                val material = if (affordable) Material.LIME_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE

                button(material, name).setLore(listOf(mini("<gray>Requires:")) + uses)
            })
            .updatePeriodically(20)
            .addClickHandler(Consumer<Click> {
                craft(player, user, id, resource, times())
                refresh()
            })
            .build()
    }

    private fun craft(player: Player, user: User, id: String, resource: ResourceConfig, times: Int) {
        val crafts = crafter.craft(player, user, id, times)
        if (crafts > 0) {
            val made = crafts * (resource.components.craftable?.output ?: 1)
            player.sendMessage(messages.crafted(made, resource.display.name))
        } else {
            player.sendMessage(messages.notEnough())
        }
    }
}
