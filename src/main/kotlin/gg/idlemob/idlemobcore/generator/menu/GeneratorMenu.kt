package gg.idlemob.idlemobcore.generator.menu

import gg.idlemob.idlemobcore.framework.menu.PageButton
import gg.idlemob.idlemobcore.framework.menu.button
import gg.idlemob.idlemobcore.framework.menu.filler
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.generator.GeneratorManager
import gg.idlemob.idlemobcore.generator.GeneratorStatus
import gg.idlemob.idlemobcore.plugin
import gg.idlemob.idlemobcore.user.User
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.util.function.Function

class GeneratorMenu(private val manager: GeneratorManager) {
    fun open(player: Player) {
        val user = plugin.users[player]
        val content = manager.slots.indices.map { slotItem(player, user, it) }

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
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
            .setTitle(mini("<dark_gray>Generators"))
            .open(player)
    }

    private fun slotItem(player: Player, user: User, slot: Int): Item {
        return Item.builder()
            .setItemProvider(Function<Player, ItemProvider> {
                val title = mini("<gold><b>Generator <slot>", Placeholder.unparsed("slot", (slot + 1).toString()))
                val state = user.getGenerator(slot)
                val blueprint = state?.let(manager::blueprint)

                val (icon, line) = when (manager.status(player, user, slot)) {
                    GeneratorStatus.LOCKED -> ItemBuilder(Material.BARRIER) to mini("<red><b>LOCKED")
                    GeneratorStatus.EMPTY, GeneratorStatus.MISSING -> ItemBuilder(Material.CRAFTER) to mini("<yellow>Waiting for blueprint")
                    GeneratorStatus.PAUSED -> ItemBuilder(Material.CRAFTER) to mini("<red>Paused")
                    GeneratorStatus.RUNNING -> {
                        val output = plugin.resources[blueprint!!.id]?.display?.toItemStack()
                        (output?.let(::ItemBuilder) ?: ItemBuilder(Material.CRAFTER)) to
                            mini("<green>Crafting <item>", Placeholder.parsed("item", blueprint.outputName))
                    }
                }

                icon.setName(title).setLore(listOf(line, Component.empty(), mini("<gray>Right-click it with a blueprint to add one,"), mini("<gray>shift + right-click to take it out.")))
            })
            .updatePeriodically(20)
            .build()
    }
}
