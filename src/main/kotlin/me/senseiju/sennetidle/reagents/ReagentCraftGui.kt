package me.senseiju.sennetidle.reagents

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.*
import me.senseiju.sennetidle.utils.extensions.asCurrencyFormat
import me.senseiju.sennetidle.utils.extensions.component
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.pow
import kotlin.math.roundToInt

private val REAGENT_CRAFT_TITLE_COMPONENT = "<dark_grey><b>Craft Reagents".component()

fun Player.openCraftReagentGui(user: User, reagent: Reagent) {
    defaultScope {
        val gui = Gui.gui()
            .title(REAGENT_CRAFT_TITLE_COMPONENT)
            .rows(5)
            .disableAllInteractions()
            .create()

        gui.filler.fill(FILLER_ITEM)

        gui.setItem(2, 5, createReagentGuiItem(user, reagent, user.getReagentAmount(reagent)))

        for (i in 1..5) {
            gui.setItem(4, 2 + i, createCraftGuiItem(user, reagent, 4.0.pow(i - 1).roundToInt()))
        }

        gui.openUpdating(2L, 2L, this) {
            this.openReagentGui()
        }
    }
}

private fun createCraftGuiItem(user: User, reagent: Reagent, amountToCraft: Int): UpdatingGuiItem {
    val material = if (user.canCraftReagent(reagent, amountToCraft)) {
        Material.GREEN_STAINED_GLASS_PANE
    } else {
        Material.RED_STAINED_GLASS_PANE
    }
    return UpdatingGuiItem(
        ItemBuilder.from(material)
            .name("<light_purple><b>Craft +$amountToCraft".component())
            .build()
    ) { e ->
        if (!e.isLeftClick) {
            return@UpdatingGuiItem
        }

        if (reagent.isCraftable() && user.canCraftReagent(reagent, amountToCraft)) {
            user.craftReagent(reagent, amountToCraft)
        }
    }.setUpdateAction {
        it.type = if (user.canCraftReagent(reagent, amountToCraft)) {
            Material.GREEN_STAINED_GLASS_PANE
        } else {
            Material.RED_STAINED_GLASS_PANE
        }
    }
}

private fun createReagentGuiItem(user: User, reagent: Reagent, amount: Int): UpdatingGuiItem {
    val item = UpdatingGuiItem(
        ItemBuilder.from(reagent.data.itemStack())
        .lore(createLore(user, reagent, amount))
        .build()
    ).setUpdateAction {
        it.lore(createLore(user, reagent, user.getReagentAmount(reagent)))
    }

    return item
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