package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sentils.extensions.primitives.toTimeFormat
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow

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
            addReagent(it.key, (it.value.mobDropBaseAmount * (1.05.pow(currentWave - it.value.mobDropUnlockWave))).toLong())
        }
    }

    fun recalculatePassiveDPS() {
        passiveDPS = reagentService.reagents.values.filter { it.damagePerSecond > 0.0 }.sumOf {
            (reagents[it.id]?.amount ?: 0) * it.damagePerSecond
        }

        // TEMPORARY
        passiveDPS += 10
    }

    private fun removeReagent(reagentId: String, amount: Long) {
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

    fun hasEnoughReagents(reagentId: String, amount: Long): Boolean {
        return reagents[reagentId]?.amount ?: 0 >= amount
    }

    /**
     * This method will remove as many possible reagents from the user. If the amount to remove is more than the user
     * has, it will set the user's amount to 0 and return the amount of items removed
     *
     * @param amount the amount to remove
     *
     * @return the amount of reagents that were removed
     */
    fun removeReagents(reagentId: String, amount: Long): Long {
        val reagent = reagents[reagentId] ?: return 0

        return if (reagent.amount - amount < 0) {
            val prevReagentAmount = reagent.amount
            reagent.amount = 0

            amount - prevReagentAmount
        } else {
            reagent.amount -= amount

            amount
        }
    }

    fun createCraftingReagentsHolder(reagent: Reagent) = UserCraftingReagentsHolder(this, reagent)

    fun getBukkitPlayer() = Bukkit.getPlayer(uuid)
}

class UserReagent(
    val id: String,
    var totalAmountCrafted: Long = 0,
    var amount: Long = 0
)

/**
 * Holds reagents which need to be kept aside for other use
 *
 * @property user the crafting user
 * @property reagentsHolding the reagents to hold
 */
abstract class UserReagentsHolder {
    abstract val user: User
    abstract val reagentsHolding: Map<String, Long>

    fun refundReagents() {
        reagentsHolding.forEach { (reagentId, amount) ->
            user.addReagent(reagentId, amount)
        }
    }
}

class UserCraftingReagentsHolder(
    override val user: User,
    val reagent: Reagent
) : UserReagentsHolder() {
    override val reagentsHolding: Map<String, Long> = reagent.craftingReagents.associate {
        it.id to user.removeReagents(it.id, it.amount)
    }

    fun addCraftedReagent() {
        user.addReagent(reagent.id, 1)
    }

    fun isFull(): Boolean {
        reagent.craftingReagents.forEach {
            if (reagentsHolding[it.id] != it.amount) {
                return false
            }
        }

        return true
    }
}