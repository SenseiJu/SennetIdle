package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.idlemobs.IdleMob
import me.senseiju.sennetidle.reagents.Reagent
import java.util.*

class User(
    val uuid: UUID,
    val reagents: HashMap<String, UserReagent>,
    var currentWave: Int
) {
    lateinit var idleMob: IdleMob
    var passiveDPS: Double = 10.0

    fun addReagent(reagentId: String, amount: Long) {
        reagents[reagentId]?.let {
            it.amount += amount
            it.totalAmountCrafted += amount
        }
    }

    fun removeReagent(reagentId: String, amount: Long) {
        reagents[reagentId]?.let {
            if (it.amount - amount < 0) {
                it.amount = 0
            } else {
                it.amount -= amount
            }
        }
    }

    fun hasCraftingReagents(reagent: Reagent): Boolean {
        reagent.craftingReagents.forEach {
            if (reagents[it.id]?.amount ?: 0 < it.amount) {
                return false
            }
        }

        return true
    }
}

class UserReagent(
    val id: String,
    var totalAmountCrafted: Long = 0,
    var amount: Long = 0
)