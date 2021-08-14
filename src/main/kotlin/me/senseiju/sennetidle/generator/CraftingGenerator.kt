package me.senseiju.sennetidle.generator

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.users.User
import org.bukkit.scheduler.BukkitRunnable

class CraftingGenerator(
    private val id: Int,
) : BukkitRunnable() {
    val activeUserCrafts = hashMapOf<User, ReagentCrafting>()

    fun startCraftingReagent() {

    }

    override fun run() {
        // TODO: Check if the currently selected reagent can be crafted. If so, start crafting
    }
}

class ReagentCrafting(
    plugin: SennetIdle,
    private val generator: CraftingGenerator,
    private val user: User,
    private val reagent: Reagent
) : BukkitRunnable() {
    private val userReagent = user.reagents[reagent.id]!!
    private var timeToCraftInTicks = reagent.calculateCraftingTime(userReagent)
    private var craftingPaused = true

    init {
        runTaskTimerAsynchronously(plugin, 0L, 1L)
    }

    override fun run() {
        if (craftingPaused) {
            if (user.hasCraftingReagents(reagent)) {
                craftingPaused = false

                resetTimeToCraft()
            } else {
                return
            }
        }

        if (timeToCraftInTicks <= 0) {
            if (user.hasCraftingReagents(reagent)) {
                reagent.craftingReagents.forEach {
                    user.removeReagent(it.id, it.amount)
                }

                user.addReagent(reagent.id, 1)
            }

            craftingPaused = true

            return
        }

        if (generator.activeUserCrafts[user] != this) {
            cancel()
            return
        }

        timeToCraftInTicks--
    }

    private fun resetTimeToCraft() {
        timeToCraftInTicks = reagent.calculateCraftingTime(userReagent)
    }
}