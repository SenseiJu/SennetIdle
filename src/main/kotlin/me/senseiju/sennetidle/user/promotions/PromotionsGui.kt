package me.senseiju.sennetidle.user.promotions

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.idlemob.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.*
import me.senseiju.sennetidle.utils.extensions.component
import org.bukkit.Material
import org.bukkit.entity.Player

private val idleMobService = serviceProvider.get<IdleMobService>()

private val TITLE_COMPONENT = "<dark_grey><b>Promotions".component()

fun Player.openPromotionsGui(user: User) {
    defaultScope {
        val gui = Gui.gui()
            .title(TITLE_COMPONENT)
            .rows(3)
            .disableAllInteractions()
            .create()

        gui.filler.fill(FILLER_ITEM)

        gui.setItem(2, 5, createPromoteItem(user))

        gui.openSync(this)
    }
}

private fun createPromoteItem(user: User): GuiItem {
    return ItemBuilder.from(Material.NETHER_STAR)
        .name(
            if (user.canPromote()) {
                "<green><b>Promotion Available"
            } else {
                "<red><b>Promotion Unavailable"
            }.component()
        )
        .lore(
            EMPTY_COMPONENT,
            "".component()
        )
        .asGuiItem {
            if (user.canPromote()) {
                user.currentWave = 1
                user.promotions++
                user.unspentUpgradePoints += 3

                idleMobService.forceNewIdleMob(user)
            } else {
                user.message(Message.PROMOTION_UNAVAILABLE)
            }
        }
}