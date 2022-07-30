package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.storage.StorageService
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.runnables.newRunnable
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import java.util.*
import kotlin.system.measureTimeMillis

private const val EXECUTE_INTERVAL_IN_TICKS = 20 * 20L

private val DATA_FAILED_TO_LOAD_COMPONENT = "<red><b>Failed to load user data, try reconnecting (If this issue persists, contact an Admin)".component()

private val storageService = serviceProvider.get<StorageService>()
private val logger = plugin.slF4JLogger

class UserCache : Listener {
    val users = hashMapOf<UUID, User>()

    private val runnable = newRunnable { saveUsers() }

    init {
        plugin.registerEvents(this)

        runnable.runTaskTimerAsynchronously(plugin, EXECUTE_INTERVAL_IN_TICKS, EXECUTE_INTERVAL_IN_TICKS)
    }

    @Synchronized
    fun saveUsers() {
        logger.info("Saving user data and cleansing cache...")

        val elapsedTime = measureTimeMillis {
            users.toMap().forEach { (playerId, user) ->
                storageService.saveUser(user)

                if (!Bukkit.getOfflinePlayer(playerId).isOnline) {
                    users.remove(playerId)
                }
            }
        }

        logger.info("Save complete (Took: ${elapsedTime}ms)")
    }

    @EventHandler
    private fun onAsyncPlayerPreLoginEvent(e: AsyncPlayerPreLoginEvent) {
        if (users.contains(e.uniqueId)) {
            return
        }

        users[e.uniqueId] = storageService.loadUser(e.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        if (users.contains(e.player.uniqueId)) {
            return
        }

        e.player.kick(DATA_FAILED_TO_LOAD_COMPONENT, PlayerKickEvent.Cause.PLUGIN)
    }
}