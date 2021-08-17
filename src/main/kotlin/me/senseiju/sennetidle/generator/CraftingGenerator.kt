package me.senseiju.sennetidle.generator

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.users.User
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import kotlin.properties.Delegates

class CraftingGenerator(
    private val plugin: SennetIdle,
    private val id: String,
    val location: Location
) {
    val activeUserCrafts = hashMapOf<User, ReagentCrafting>()

    fun startReagentCrafting(user: User, reagent: Reagent) {
        activeUserCrafts[user] = ReagentCrafting(plugin, this, user, reagent)
    }
}

class ReagentCrafting(
    plugin: SennetIdle,
    private val generator: CraftingGenerator,
    private val user: User,
    val reagent: Reagent
) : BukkitRunnable() {
    val userReagent = user.reagents[reagent.id]!!
    private var timeRemainingToCraftInTicks by Delegates.notNull<Float>()
    private var craftingPaused = true

    init {
        resetTime()

        runTaskTimerAsynchronously(plugin, 0L, 1L)
    }

    fun getTimeRemaining() = timeRemainingToCraftInTicks / 20

    override fun run() {
        if (generator.activeUserCrafts[user] != this) {
            cancel()
            return
        }

        if (craftingPaused) {
            if (user.hasCraftingReagents(reagent)) {
                resetTime()

                craftingPaused = false
            } else {
                return
            }
        }

        if (timeRemainingToCraftInTicks <= 0) {
            if (user.hasCraftingReagents(reagent)) {
                reagent.craftingReagents.forEach {
                    user.removeReagent(it.id, it.amount)
                }

                user.addReagent(reagent.id, 1)
            }

            craftingPaused = true

            return
        }

        timeRemainingToCraftInTicks--
    }

    private fun resetTime() {
        timeRemainingToCraftInTicks = reagent.calculateCraftingTimeInTicks(userReagent)
    }
}