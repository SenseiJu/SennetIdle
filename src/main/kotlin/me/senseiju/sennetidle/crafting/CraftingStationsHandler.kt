package me.senseiju.sennetidle.crafting

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.users.UserService
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.serializers.LocationSerializer
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent

private val userService = serviceProvider.get<UserService>()

class CraftingStationsHandler(
    private val plugin: SennetIdle,
    private val id: String,
    private val location: Location
) : Listener {
    val userStations = hashMapOf<User, UserCraftingStation>()

    init {
        plugin.registerEvents(this)

        plugin.server.onlinePlayers.forEach {
            addNewUserStation(userService.getUser(it.uniqueId))
        }
    }

    fun addNewUserStation(user: User): UserCraftingStation {
        userStations.putIfAbsent(user, UserCraftingStation(user, location))

        return userStations[user]!!
    }

    fun disposeUserStation(user: User) {
        userStations[user]?.dispose()
    }

    fun dispose() {
        HandlerList.unregisterAll(this)

        userStations.values.forEach {
            it.dispose()
        }
    }

    fun toJson() = Json.encodeToString(SerializableCraftingStation(id, location))

    @EventHandler
    private fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        userService.getUser(e.player.uniqueId).let {
            userStations[it]?.showHologram()
        }
    }

    @EventHandler
    private fun onGeneratorRightClickEvent(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_BLOCK) return
        if (e.clickedBlock?.location?.equals(location) == false) return

        //TODO Open station gui

        e.isCancelled = true
    }
}

@Serializable
data class SerializableCraftingStation(
    val id: String,
    @Serializable(LocationSerializer::class) val location: Location
)