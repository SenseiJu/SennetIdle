package me.senseiju.sennetidle.user.promotions

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.idlemob.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.*
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sennetidle.utils.extensions.playSound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder

private val idleMobService = serviceProvider.get<IdleMobService>()

private val TITLE_COMPONENT = "<dark_grey><b>Promotions".component()

fun Player.openPromotionsGui(user: User) {
    val gui = Gui.empty(9, 3)
    gui.background = FILLER_ITEM

    gui.setItem(4, 1, createPromoteItem(user))

    openGui(TITLE_COMPONENT, gui)
}

private fun createPromoteItem(user: User): Item {
    return guiItem(
        provider = {
            ItemBuilder(Material.NETHER_STAR)
                .setName("<light_purple><b>Promotion".component())
                .setLore(createPromoteItemLore(user))
        },
        onClick = {
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
    )
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