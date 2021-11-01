package me.senseiju.sennetidle.crafting

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.users.User
import me.senseiju.sentils.serializers.LocationSerializer
import me.senseiju.sentils.service.Service
import org.bukkit.Location
import java.io.File
import java.util.concurrent.locks.ReentrantLock

class CraftingService(private val plugin: SennetIdle) : Service() {
    private val craftingStationsFile = File(plugin.dataFolder, "craftingStations.json")

    private val craftingStationsHandlers = hashMapOf<String, CraftingStationsHandler>()
    private val userLocks = hashMapOf<User, ReentrantLock>()

    init {
        loadCraftingStations()
    }

    override fun onDisable() {
        saveCraftingStations()
        craftingStationsHandlers.values.forEach {
            it.dispose()
        }
    }

    fun createNewCraftingStationsHandler(id: String, location: Location): Boolean {
        if (craftingStationsHandlers.containsKey(id)) return false

        craftingStationsHandlers[id] = CraftingStationsHandler(plugin, id, location)

        return true
    }

    fun getNextCraftingReagent(reagent: Reagent, user: User): CraftingReagent? {
        val lock = userLocks.getOrPut(user) { ReentrantLock() }
        if (!lock.tryLock()) return null
        if (!user.hasCraftingReagents(reagent)) return null

        val holder = CraftingReagent(user, user.createCraftingReagentsHolder(reagent))

        lock.unlock()

        return holder
    }

    fun getUserCraftingStations(user: User): Map<String, UserCraftingStation> {
        return craftingStationsHandlers.filter { it.value.userStations[user] != null }.mapValues { it.value.userStations[user]!! }
    }

    fun disposeUserCraftingStations(user: User) {
        craftingStationsHandlers.values.forEach {
            it.disposeUserStation(user)
        }
    }

    fun getCraftingStationHandler(id: String) = craftingStationsHandlers[id]

    private fun loadCraftingStations() {
        if (!craftingStationsFile.exists()) return

        val stationsList = Json.decodeFromString<List<String>>(craftingStationsFile.readText()).map {
            Json.decodeFromString<SerializableCraftingGenerator>(it)
        }

        stationsList.forEach {
            if (!craftingStationsHandlers.containsKey(it.id)) {
                createNewCraftingStationsHandler(it.id, it.location)
            }
        }
    }

    private fun saveCraftingStations() {
        if (!craftingStationsFile.exists()) {
            craftingStationsFile.createNewFile()
        }

        val stationsList = craftingStationsHandlers.values.map { it.toJson() }

        craftingStationsFile.writeText(Json.encodeToString(stationsList))
    }
}

@Serializable
data class SerializableCraftingGenerator(
    val id: String,
    @Serializable(LocationSerializer::class) val location: Location
)