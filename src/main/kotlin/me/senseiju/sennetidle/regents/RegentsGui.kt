package me.senseiju.sennetidle.regents

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

private val titleComponent = text("Regents", RED, TextDecoration.BOLD)
private val regentService = serviceProvider.get<RegentService>()

fun Player.openRegentsGui(user: User) {
    createRegentsGui(user).open(this)
}

private fun createRegentsGui(user: User): ScrollingGui {
    val gui = Gui.scrolling(ScrollType.HORIZONTAL)
        .title(titleComponent)
        .rows(3)
        .pageSize(21)
        .create()

    gui.filler.fillBetweenPoints(1, 1, 3, 1, fillerItem())
    gui.filler.fillBetweenPoints(1, 9, 3, 9, fillerItem())

    gui.setPreviousItem(2, 1)
    gui.setNextItem(2, 9)

    gui.disableAllInteractions()

    populateRegentsIntoGui(gui, user)

    return gui
}

private fun populateRegentsIntoGui(gui: ScrollingGui, user: User) {
    regentService.regents.values.reversed().forEach {
        gui.addItem(GuiItem(it.displayItem(user.regents[it.id]!!)))
    }
}