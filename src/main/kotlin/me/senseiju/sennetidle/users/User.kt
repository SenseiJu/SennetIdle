package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sentils.extensions.primitives.toTimeFormat
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

private val reagentService = serviceProvider.get<ReagentService>()

class User(
    val uuid: UUID,
    val reagents: HashMap<String, UserReagent>,
    var currentWave: Int
) {
    var passiveDPS = 0.0
        private set
    var lastSeen = System.currentTimeMillis()

    fun timeLoggedOut() = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastSeen).toTimeFormat()

    fun addReagent(reagentId: String, amount: Long) {
        reagents[reagentId]?.let {
            it.amount += amount
            it.totalAmountCrafted += amount
        }

        if (reagentService.reagents[reagentId]?.damagePerSecond ?: 0.0 > 0.0) {
            recalculatePassiveDPS()
        }
    }

    /**
     * Attempts to craft the reagent if the user has met the crafting requirements
     *
     * @return true if crafted successfully otherwise false
     */
    fun tryCraftReagent(reagent: Reagent): Boolean {
        if (hasCraftingReagents(reagent)) {
            reagent.craftingReagents.forEach {
                removeReagent(it.id, it.amount)
            }
            addReagent(reagent.id, 1)
            return true
        }

        return false
    }

    fun rewardWaveReagent() {
        reagentService.reagents.filter { it.value.mobDropUnlockWave in 1..currentWave }.forEach {
            addReagent(it.key, 5)
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

    fun getBukkitPlayer() = Bukkit.getPlayer(uuid)
}

class UserReagent(
    val id: String,
    var totalAmountCrafted: Long = 0,
    var amount: Long = 0
)