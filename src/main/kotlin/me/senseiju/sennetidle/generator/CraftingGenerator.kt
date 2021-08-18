package me.senseiju.sennetidle.generator

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.users.User
import me.senseiju.sentils.serializers.LocationSerializer
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import kotlin.properties.Delegates

class CraftingGenerator(
    private val plugin: SennetIdle,
    private val id: String,
    val location: Location
) {
    val activeUserCrafts = hashMapOf<User, CraftingReagent>()

    fun startReagentCrafting(user: User, reagent: Reagent) {
        activeUserCrafts[user] = CraftingReagent(plugin, this, user, reagent)
    }

    fun toJson() = Json.encodeToString(SerializableCraftingGenerator(id, location))
}

@Serializable
data class SerializableCraftingGenerator(
    val id: String,
    @Serializable(LocationSerializer::class) val location: Location
)

class CraftingReagent(
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