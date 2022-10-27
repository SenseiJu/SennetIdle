package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.reagents.Reagent
import java.util.*

interface ReagentHolder {
    val reagents: EnumMap<Reagent, Int>
        get() = reagents.toMap(EnumMap(Reagent::class.java))

    fun getReagentAmount(reagent: Reagent): Int {
        return reagents.getOrPut(reagent) { 0 }
    }
    fun addReagent(reagent: Reagent, amount: Int) {
        reagents.putIfAbsent(reagent, 0)
        reagents[reagent] = (reagents[reagent]!! + amount).coerceAtLeast(0)

        onAddReagent(reagent, amount)
    }

    fun onAddReagent(reagent: Reagent, amount: Int) {}

    fun hasReagent(reagent: Reagent, amount: Int): Boolean {
        return reagents.getOrPut(reagent) { 0 } >= amount
    }

    fun canCraftReagent(reagent: Reagent, amountToCraft: Int): Boolean {
        if (!reagent.isCraftable()) {
            return false
        }

        reagent.asCraftable()
            .reagentRequirements
            .forEach { (requiredReagent, requiredAmount) ->
                if (!hasReagent(requiredReagent, requiredAmount * amountToCraft)) {
                    return false
                }
            }

        return true
    }

    fun craftReagent(reagent: Reagent, amountToCraft: Int) {
        reagent.asCraftable()
            .reagentRequirements
            .forEach { (requiredReagent, requiredAmount) ->
                addReagent(requiredReagent, -requiredAmount * amountToCraft)
            }

        addReagent(reagent, amountToCraft)
    }
}