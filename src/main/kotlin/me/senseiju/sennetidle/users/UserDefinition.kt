package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.idlemobs.IdleMob
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.serviceProvider
import java.util.*
import java.util.concurrent.locks.ReentrantLock

private val reagentService = serviceProvider.get<ReagentService>()

class User(
    val uuid: UUID,
    val reagents: HashMap<String, UserReagent>,
    var currentWave: Int
) {
    lateinit var idleMob: IdleMob
    var passiveDPS = 0.0
        private set

    fun addReagent(reagentId: String, amount: Long) {
        reagents[reagentId]?.let {
            it.amount += amount
            it.totalAmountCrafted += amount
        }

        if (reagentService.reagents[reagentId]?.damagePerSecond ?: 0.0 > 0.0) {
            recalculatePassiveDPS()
        }
    }

    fun recalculatePassiveDPS() {
        passiveDPS = reagentService.reagents.values.filter { it.damagePerSecond > 0.0 }.sumOf {
            (reagents[it.id]?.amount ?: 0) * it.damagePerSecond
        }

        // TEMPORARY
        passiveDPS += 10
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

    /**
     * Lock used since generators are async and it causes weird issues
     */
    private var hasCraftingReagentsLock = ReentrantLock()
    fun hasCraftingReagents(reagent: Reagent): Boolean {
        if (!hasCraftingReagentsLock.tryLock()) return false

        reagent.craftingReagents.forEach {
            if (reagents[it.id]?.amount ?: 0 < it.amount) {
                hasCraftingReagentsLock.unlock()

                return false
            }
        }

        hasCraftingReagentsLock.unlock()

        return true
    }
}

class UserReagent(
    val id: String,
    var totalAmountCrafted: Long = 0,
    var amount: Long = 0
)