package me.senseiju.sennetidle.generator

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.utils.*
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

private val plugin = JavaPlugin.getPlugin(SennetIdle::class.java)
private val reagentService = serviceProvider.get<ReagentService>()

fun Player.openGeneratorGui(user: User, generator: CraftingGenerator) {
    val gui = Gui.gui()
        .disableAllInteractions()
        .title(text("Generator", RED, TextDecoration.BOLD))
        .rows(3)
        .create()

    gui.filler.fillBorder(fillerItem())

    val displayItem = {
        generator.activeUserCrafts[user]?.let { it.reagent.displayItem(it.userReagent) } ?: ItemStack(Material.BARRIER)
    }

    val updatingGuiItem = UpdatingGuiItem(displayItem()) {
        openSelectNewCraftingReagentGui(user, generator)
    }.setUpdatingRunnable(1L, 1L) {
        gui.updateItem(2, 5,
            ItemBuilder.from(displayItem())
                .lore {
                    it.add(newline())
                    it.add(
                        text(
                            "Craft time remaining: ",
                            AQUA
                        ).append(
                            text(
                                generator.activeUserCrafts[user]?.getTimeRemainingString() ?: "0F",
                                GRAY
                            )
                        )
                    )
                }
                .build()
        )
    }

    gui.setItem(2, 5, updatingGuiItem)
    gui.openUpdating(plugin, this)
}

private fun Player.openSelectNewCraftingReagentGui(user: User, generator: CraftingGenerator) {
    val gui = Gui.scrolling(ScrollType.HORIZONTAL)
        .title(text(""))
        .disableAllInteractions()
        .rows(3)
        .pageSize(21)
        .create()

    gui.filler.fillBetweenPoints(1, 1, 3, 1, fillerItem())
    gui.filler.fillBetweenPoints(1, 9, 3, 9, fillerItem())

    gui.setPreviousItem(2, 1)
    gui.setNextItem(2, 9)

    reagentService.reagents.values.filter { reagent ->
        reagent.craftingReagents.isNotEmpty() && generator.activeUserCrafts[user]?.reagent != reagent
    }.forEach { reagent ->
        gui.addItem(
            ItemBuilder.from(reagent.material)
                .name(text(reagent.displayName, YELLOW))
                .asGuiItem {
                    generator.startReagentCrafting(user, reagent)

                    this.openGeneratorGui(user, generator)
                }
        )
    }

    gui.open(this)
}