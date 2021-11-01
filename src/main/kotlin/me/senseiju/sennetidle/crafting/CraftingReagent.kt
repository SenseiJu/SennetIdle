package me.senseiju.sennetidle.crafting

import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.users.UserCraftingReagentsHolder

class CraftingReagent(
    user: User,
    private val userCraftingReagentsHolder: UserCraftingReagentsHolder
) {
    private var timeRemainingInTicks = userCraftingReagentsHolder.reagent.calculateCraftingTimeInTicks(
        user.reagents[userCraftingReagentsHolder.reagent.id]?.totalAmountCrafted ?: 0)

    var finished = false

    fun timeRemainingString() = String.format("%.1f", (timeRemainingInTicks / 20).toDouble().coerceAtLeast(0.0))

    fun getReagent() = userCraftingReagentsHolder.reagent

    fun subtractTimeRemaining(ticks: Float = 1F) {
        timeRemainingInTicks -= ticks

        if (timeRemainingInTicks <= 0) {
            userCraftingReagentsHolder.addCraftedReagent()
            finished = true
        }
    }
}