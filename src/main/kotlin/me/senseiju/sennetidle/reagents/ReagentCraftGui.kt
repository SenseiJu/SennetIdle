package me.senseiju.sennetidle.reagents

import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.*
import me.senseiju.sennetidle.utils.extensions.asCurrencyFormat
import me.senseiju.sennetidle.utils.extensions.component
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import kotlin.math.pow
import kotlin.math.roundToInt

private val REAGENT_CRAFT_TITLE_COMPONENT = "<dark_grey><b>Craft Reagents".component()

fun Player.openCraftReagentGui(user: User, reagent: Reagent) {
    val gui = Gui.empty(9, 5)
    gui.background = FILLER_ITEM

    gui.setItem(4, 1, createReagentGuiItem(user, reagent))

    for (i in 1..5) {
        gui.setItem(1 + i, 3, createCraftGuiItem(user, reagent, 4.0.pow(i - 1).roundToInt()))
    }

    openGui(REAGENT_CRAFT_TITLE_COMPONENT, gui) {
        openReagentGui()
    }
}

private fun createCraftGuiItem(user: User, reagent: Reagent, amountToCraft: Int): Item {
    return guiItem(
        updatePeriod = 2,
        provider = {
            val material = if (user.canCraftReagent(reagent, amountToCraft)) {
                Material.GREEN_STAINED_GLASS_PANE
            } else {
                Material.RED_STAINED_GLASS_PANE
            }

            ItemBuilder(material).setName("<light_purple><b>Craft +$amountToCraft".component())
        },
        onClick = { click ->
            if (click.clickType.isLeftClick && reagent.isCraftable() && user.canCraftReagent(reagent, amountToCraft)) {
                user.craftReagent(reagent, amountToCraft)
            }
        }
    )
}

private fun createReagentGuiItem(user: User, reagent: Reagent): Item {
    return guiItem(
        updatePeriod = 2,
        provider = { ItemBuilder(reagent.data.itemStack()).setLore(createLore(user, reagent, user.getReagentAmount(reagent))) }
    )
}

private fun createLore(user: User, reagent: Reagent, amount: Int): List<Component> {
    val lore = arrayListOf(EMPTY_COMPONENT)

    lore.add("<aqua>Amount: <yellow>${amount.asCurrencyFormat()}".component())

    if (reagent.isDamaging()) {
        reagent.asDamageable().createLore(lore)
    }

    if (reagent.isCraftable()) {
        reagent.asCraftable().createLore(user, lore)
    }

    return lore
}
