package me.senseiju.sennetidle.reagents

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import me.senseiju.sennetidle.reagents.reagentData.CraftableReagent
import me.senseiju.sennetidle.reagents.reagentData.DamagingReagent
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.UpdatingGuiItem
import me.senseiju.sennetidle.utils.defaultScope
import me.senseiju.sennetidle.utils.extensions.asCurrencyFormat
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sennetidle.utils.openUpdating
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

private val EMPTY_COMPONENT = "".component()
private val TITLE_COMPONENT = "<light_purple>Reagents".component()

fun Player.openReagentGui() {
    defaultScope {
        val gui = Gui.gui()
            .title(TITLE_COMPONENT)
            .rows(6)
            .disableAllInteractions()
            .create()

        val user = userService.getUser(this.uniqueId)

        Reagent.values().forEach { reagent ->
            gui.addItem(createReagentGuiItem(user, reagent, user.reagents.getOrPut(reagent) { 0 }))
        }

        gui.openUpdating(2L, 2L, this)
    }
}

private fun createReagentGuiItem(user: User, reagent: Reagent, amount: Int): UpdatingGuiItem {
    val item = UpdatingGuiItem(ItemBuilder.from(reagent.toItemStack())
        .lore(createLore(user, reagent, amount))
        .build()
    ) { e ->
        if (reagent.isCraftable() && user.canCraftReagent(reagent, 1)) {
            user.craftReagent(reagent, 1)

            e.currentItem?.editMeta { meta ->
                meta.lore(createLore(user, reagent, user.reagents.getOrDefault(reagent, 0)))
            }
        }
    }.setUpdateAction {
        it.lore(createLore(user, reagent, user.reagents.getOrDefault(reagent, 0)))
    }

    return item
}

private fun createLore(user: User, reagent: Reagent, amount: Int): List<Component> {
    val lore = arrayListOf(EMPTY_COMPONENT)

    lore.add("<aqua>Amount: <yellow>${amount.asCurrencyFormat()}".component())

    if (reagent.isDamaging()) {
        reagent.createDamagingLore(lore)
    }

    if (reagent.isCraftable()) {
        reagent.createCraftableLore(user, lore)
    }

    return lore
}

private val CRAFTING_REQUIREMENTS_COMPONENT = "<aqua>Crafting Requirements: ".component()
private val CLICK_TO_CRAFT_COMPONENT = "<white>Left-Click <gray>to craft".component()
private val GREEN_TICK_COMPONENT = "<green><b>[✔]".component()
private val RED_CROSS_COMPONENT = "<red><b>[❌]".component()
private fun Reagent.createCraftableLore(user: User, lore: MutableList<Component> = arrayListOf()): MutableList<Component> {
    val data = dataAs<CraftableReagent>()

    lore.add(EMPTY_COMPONENT)
    lore.add(CRAFTING_REQUIREMENTS_COMPONENT)

    data.reagentRequirements.forEach { (reqReagent, reqAmount) ->
        val reqComponent = "  <yellow>x$reqAmount ${reqReagent.displayName} ".component()

        if (user.hasReagent(reqReagent, reqAmount)) {
            lore.add(reqComponent.append(GREEN_TICK_COMPONENT))
        } else {
            lore.add(reqComponent.append(RED_CROSS_COMPONENT))
        }
    }

    lore.add(EMPTY_COMPONENT)
    lore.add(CLICK_TO_CRAFT_COMPONENT)

    return lore
}

private fun Reagent.createDamagingLore(lore: MutableList<Component> = arrayListOf()): MutableList<Component> {
    val data = dataAs<DamagingReagent>()

    lore.add(EMPTY_COMPONENT)
    lore.add("<aqua>Damage Per Second (DPS): <yellow>${data.damagePerSecond.asCurrencyFormat()}".component())

    return lore
}