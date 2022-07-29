package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.reagents.reagentData.CraftableReagent
import me.senseiju.sennetidle.reagents.reagentData.DamagingReagent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class User(
    val playerId: UUID,
    var currentWave: Int,
    val reagents: EnumMap<Reagent, Int>
) {
    var idleMobId: UUID? = null
    var dps: Long = 35
        private set

    init {
        Reagent.damaging.forEach {
            dps += it.dataAs<DamagingReagent>().damagePerSecond * reagents.getOrDefault(it, 0)
        }
    }

    companion object {
        fun new(playerId: UUID): User {
            return User(playerId, 1, Reagent.emptyUserMap())
        }
    }

    fun getPlayer(): Player? {
        return Bukkit.getPlayer(playerId)
    }

    fun addReagent(reagent: Reagent, amount: Int) {
        reagents.putIfAbsent(reagent, 0)
        reagents[reagent] = reagents[reagent]!! + amount

        if (reagent.isDamaging()) {
            dps += reagent.dataAs<DamagingReagent>().damagePerSecond * amount
        }
    }

    fun hasReagent(reagent: Reagent, amount: Int): Boolean {
        return reagents.getOrDefault(reagent, 0) >= amount
    }

    fun canCraftReagent(reagent: Reagent, amountToCraft: Int): Boolean {
        if (!reagent.isCraftable()) {
            return false
        }

        reagent.dataAs<CraftableReagent>()
            .reagentRequirements
            .forEach { (requiredReagent, requiredAmount) ->
                if (!hasReagent(requiredReagent, requiredAmount * amountToCraft)) {
                    return false
                }
            }

        return true
    }

    fun craftReagent(reagent: Reagent, amountToCraft: Int) {
        reagent.dataAs<CraftableReagent>()
            .reagentRequirements
            .forEach { (requiredReagent, requiredAmount) ->
                addReagent(requiredReagent, -requiredAmount * amountToCraft)
            }

        addReagent(reagent, amountToCraft)
    }

    /**
     * Executes the block if the [Player] is available otherwise nothing will happen
     */
    fun withPlayer(block: (Player) -> Unit) {
        getPlayer()?.let { block(it) }
    }
}