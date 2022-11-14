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
import me.senseiju.sentils.extensions.entity.playSound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Material
import org.bukkit.Sound
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
        .name("<light_purple><b>Promotion".component())
        .lore(createPromoteItemLore(user))
        .asGuiItem {
            if (user.canPromote()) {
                user.promote()

                val promotionSuccessTitle = Title.title(
                    "<green><b><obf>||</obf> PROMOTION ${user.promotions} <obf>||</obf>".component(),
                    "<grey>You have been promoted!".component(),
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(20))
                )
                user.withPlayer {
                    it.showTitle(promotionSuccessTitle)
                    it.playSound(Sound.ENTITY_PLAYER_LEVELUP)
                }

                idleMobService.forceNewIdleMob(user)
            } else {
                user.message(Message.PROMOTION_UNAVAILABLE)
            }
        }
}
private fun createPromoteItemLore(user: User): List<Component> {
    val lore = mutableListOf<Component>()

    lore.add(EMPTY_COMPONENT)
    lore.add("<aqua>Promotion Requirements: ".component())

    lore.add("  <yellow>Wave ${user.nextPromotionWave()} ${tickComponent(user.canPromote())}".component())

    lore.add(EMPTY_COMPONENT)
    lore.add("<aqua>Rewards: ".component())
    lore.add("  <yellow>x3 Upgrade Points".component())

    lore.add(EMPTY_COMPONENT)
    lore.add("<red>WARNING <grey>Current wave will be reset".component())

    return lore
}