package me.senseiju.sennetidle.generator

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.generator.holograms.updateGeneratorHologram
import me.senseiju.sennetidle.generator.holograms.updateTimeRemainingLine
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.users.User
import org.bukkit.scheduler.BukkitRunnable
import kotlin.properties.Delegates

class CraftingReagent(
    plugin: SennetIdle,
    private val generator: CraftingGenerator,
    private val user: User,
    val reagent: Reagent
) : BukkitRunnable() {
    val userReagent = user.reagents[reagent.id]!!
    private var timeRemainingToCraftInTicks by Delegates.notNull<Float>()
    var craftingPaused = true

    init {
        resetTime()

        runTaskTimerAsynchronously(plugin, 0L, 1L)
    }

    fun getTimeRemainingString() = String.format("%.1f", timeRemainingToCraftInTicks / 20)

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
                generator.userHolograms[user]?.updateTimeRemainingLine(this)

                return
            }
        }

        if (timeRemainingToCraftInTicks <= 0) {
            user.tryCraftReagent(reagent)
            craftingPaused = true
            return
        }

        timeRemainingToCraftInTicks--

        generator.userHolograms[user]?.updateTimeRemainingLine(this)
    }

    private fun resetTime() {
        timeRemainingToCraftInTicks = reagent.calculateCraftingTimeInTicks(userReagent)
    }
}