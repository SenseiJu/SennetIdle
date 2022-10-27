package me.senseiju.sennetidle.reagents

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import me.senseiju.sennetidle.reagents.reagentData.CraftableReagent
import me.senseiju.sennetidle.reagents.reagentData.DamagingReagent
import me.senseiju.sennetidle.reagents.reagentData.DroppableReagent
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.*
import me.senseiju.sennetidle.utils.extensions.asCurrencyFormat
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sentils.extensions.events.player
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

private val REAGENT_TITLE_COMPONENT = "<dark_grey><b>Reagents".component()

fun Player.openReagentGui() {
    defaultScope {
        val gui = Gui.gui()
            .title(REAGENT_TITLE_COMPONENT)
            .rows(6)
            .disableAllInteractions()
            .create()

        val user = userService.getUser(this.uniqueId)

        Reagent.values().forEach { reagent ->
            gui.addItem(createReagentGuiItem(user, reagent, user.getReagentAmount(reagent)))
        }

        gui.openUpdating(20L, 20L, this)
    }
}

private fun createReagentGuiItem(user: User, reagent: Reagent, amount: Int): UpdatingGuiItem {
    val item = UpdatingGuiItem(ItemBuilder.from(reagent.data.itemStack())
        .lore(createLore(user, reagent, amount))
        .build()
    ) { e ->
        if (!e.isLeftClick) {
            return@UpdatingGuiItem
        }

        if (reagent.isCraftable() && user.hasEnoughPromotions(reagent)) {
            e.player.openCraftReagentGui(user, reagent)
        }
    }.setUpdateAction {
        it.lore(createLore(user, reagent, user.getReagentAmount(reagent)))
    }

    return item
}

private fun createLore(user: User, reagent: Reagent, amount: Int): List<Component> {
    val lore = arrayListOf(EMPTY_COMPONENT)

    if (!user.hasEnoughPromotions(reagent)) {
        lore.add("<red>LOCKED <grey>You can unlock this reagent by reaching <white>${reagent.data.promotionUnlock} promotion(s)".component())
        return lore
    }

    if (reagent.isDroppable()) {
        reagent.asDroppable().createLore(user, lore)
    }
    lore.add("<aqua>Amount: <yellow>${amount.asCurrencyFormat()}".component())

    if (reagent.isDamaging()) {
        reagent.asDamageable().createLore(lore)
    }

    if (reagent.isCraftable()) {
        reagent.asCraftable().createLore(user, lore)

        lore.add(EMPTY_COMPONENT)
        lore.add(CLICK_TO_CRAFT_COMPONENT)
    }

    return lore
}

private val CRAFTING_REQUIREMENTS_COMPONENT = "<aqua>Crafting Requirements: ".component()
private val CLICK_TO_CRAFT_COMPONENT = "<white>Left-Click <gray>to craft".component()
fun CraftableReagent.createLore(user: User, lore: MutableList<Component>): MutableList<Component> {
    lore.add(EMPTY_COMPONENT)
    lore.add(CRAFTING_REQUIREMENTS_COMPONENT)

    reagentRequirements.forEach { (reqReagent, reqAmount) ->
        val reqComponent = "  <yellow>x$reqAmount ${reqReagent.name} ".component()

        if (user.hasReagent(reqReagent, reqAmount)) {
            lore.add(reqComponent.append(GREEN_TICK_COMPONENT))
        } else {
            lore.add(reqComponent.append(RED_CROSS_COMPONENT))
        }
    }

    return lore
}

fun DamagingReagent.createLore(lore: MutableList<Component>): MutableList<Component> {
    lore.add(EMPTY_COMPONENT)
    lore.add("<aqua>Damage Per Second (DPS): <yellow>${damagePerSecond.asCurrencyFormat()}".component())
    if (bossOnly) {
        lore.add("<white><b>Only affects bosses".component())
    }

    return lore
}

fun DroppableReagent.createLore(user: User, lore: MutableList<Component>): MutableList<Component> {
    lore.add("<grey>This reagent can be obtained from mob drops".component())
    lore.add(EMPTY_COMPONENT)

    val unlockComponent = "<aqua>Wave Unlock: <yellow>$waveUnlock ".component()
    if (user.currentWave >= waveUnlock) {
        lore.add(unlockComponent.append(GREEN_TICK_COMPONENT))
    } else {
        lore.add(unlockComponent.append(RED_CROSS_COMPONENT))
    }

    return lore
}