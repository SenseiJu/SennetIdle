package me.senseiju.sennetidle.reagents

import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.ScrollingGui
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.utils.fillerItem
import me.senseiju.sennetidle.utils.setNextItem
import me.senseiju.sennetidle.utils.setPreviousItem
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

private val titleComponent = text("Reagents", RED, TextDecoration.BOLD)
private val reagentService = serviceProvider.get<ReagentService>()

fun Player.openReagentsGui(user: User) {
    val gui = Gui.scrolling(ScrollType.HORIZONTAL)
        .disableAllInteractions()
        .title(titleComponent)
        .rows(3)
        .pageSize(21)
        .create()

    gui.filler.fillBetweenPoints(1, 1, 3, 1, fillerItem())
    gui.filler.fillBetweenPoints(1, 9, 3, 9, fillerItem())

    gui.setPreviousItem(2, 1)
    gui.setNextItem(2, 9)

    reagentService.reagents.values.reversed().forEach {
        gui.addItem(GuiItem(it.displayItem(user.reagents[it.id]!!)))
    }

    gui.open(this)
}