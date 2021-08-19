package me.senseiju.sennetidle.generator

import com.gmail.filoghost.holographicdisplays.api.Hologram
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.generator.holograms.updateGeneratorHologram
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.users.User
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.serializers.LocationSerializer
import org.bukkit.Location
import org.bukkit.event.Listener

class CraftingGenerator(
    private val plugin: SennetIdle,
    val id: String,
    val location: Location
) : Listener {
    val activeUserCrafts = hashMapOf<User, CraftingReagent>()
    val userHolograms = hashMapOf<User, Hologram>()

    init {
        plugin.registerEvents(this)
    }

    fun startReagentCrafting(user: User, reagent: Reagent) {
        val craftingReagent = CraftingReagent(plugin, this, user, reagent)

        activeUserCrafts[user] = craftingReagent
        userHolograms[user]?.updateGeneratorHologram(craftingReagent)
    }

    fun dispose() {
        userHolograms.values.forEach {
            it.delete()
        }

        activeUserCrafts.values.forEach {
            it.cancel()
        }
    }

    fun toJson() = Json.encodeToString(SerializableCraftingGenerator(id, location))
}

@Serializable
data class SerializableCraftingGenerator(
    val id: String,
    @Serializable(LocationSerializer::class) val location: Location
)