package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.runnables.newRunnable
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

private const val EXECUTE_INTERVAL_IN_TICKS = 120 * 20L

class UserCache : Listener {
    val users = hashMapOf<UUID, User>()

    private val runnable = newRunnable { saveUsers() }

    init {
        plugin.registerEvents(this)

        runnable.runTaskTimerAsynchronously(plugin, EXECUTE_INTERVAL_IN_TICKS, EXECUTE_INTERVAL_IN_TICKS)
    }

    fun saveUsers() {
        // Add saving

        users.keys.filterNot { playerId ->
            Bukkit.getOfflinePlayer(playerId).isOnline
        }.forEach {
            users.remove(it)
        }
    }

    private fun loadUser(playerId: UUID): User {
        return User.new(playerId)
    }

    @EventHandler
    private fun onAsyncPlayerPreLoginEvent(e: AsyncPlayerPreLoginEvent) {
        if (users.contains(e.uniqueId)) {
            return
        }

        users[e.uniqueId] = loadUser(e.uniqueId)
    }

    // KEEP FOR NOW :)
    @EventHandler
    private fun onNasrinnJoinEvent(e: PlayerJoinEvent) {
        if (e.player.uniqueId == UUID.fromString("2852170a-8a30-4cfb-a276-70a8fb5df090")) {
            e.player.sendMessage("Your FAT LOL")
        }
    }
}