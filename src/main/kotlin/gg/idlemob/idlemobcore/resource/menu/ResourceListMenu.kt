package gg.idlemob.idlemobcore.resource.menu

import gg.idlemob.idlemobcore.framework.menu.PageButton
import gg.idlemob.idlemobcore.framework.menu.button
import gg.idlemob.idlemobcore.framework.menu.filler
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.plugin
import gg.idlemob.idlemobcore.resource.ResourceConfig
import gg.idlemob.idlemobcore.resource.ResourceCrafter
import gg.idlemob.idlemobcore.resource.ResourceRegistry
import gg.idlemob.idlemobcore.resource.isUnlockedFor
import gg.idlemob.idlemobcore.user.User
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer
import java.util.function.Function

class ResourceListMenu(private val resources: ResourceRegistry, crafter: ResourceCrafter) {
    private val lore = ResourceLore(resources)
    private val craftMenu = ResourceCraftMenu(resources, crafter, lore, ::open)

    fun open(player: Player) {
        val user = plugin.users[player]

        val content = resources.entries.entries
            .sortedBy { it.value.sortOrder }
            .map { (id, resource) -> resourceItem(player, user, id, resource) }

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "# # # < # > # # #",
            )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('#', filler())
            .addIngredient('<', PageButton(button(Material.ARROW, mini("<yellow>Previous page")), filler(), -1))
            .addIngredient('>', PageButton(button(Material.ARROW, mini("<yellow>Next page")), filler(), 1))
            .setContent(content)
            .build()

        Window.builder()
            .setUpperGui(gui)
            .setTitle(mini("<dark_gray>Resources"))
            .open(player)
    }

    private fun resourceItem(player: Player, user: User, id: String, resource: ResourceConfig): Item {
        return Item.builder()
            .setItemProvider(Function<Player, ItemProvider> {
                ItemBuilder(resource.display.toItemStack()).setName(resource.display.displayName()).setLore(lore.lore(user, id, resource, showClickHint = true))
            })
            .updatePeriodically(20)
            .addClickHandler(Consumer<Click> {
                if (resource.components.craftable != null && resource.isUnlockedFor(user)) {
                    craftMenu.open(player, user, id, resource)
                }
            })
            .build()
    }
}
