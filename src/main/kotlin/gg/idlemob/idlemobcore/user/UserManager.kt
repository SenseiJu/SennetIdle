package gg.idlemob.idlemobcore.user

import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.framework.coroutines.PluginScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

class UserManager(
    private val plugin: IdleMobCore,
    private val scope: PluginScope,
    private val repository: UserRepository,
) : Listener {
    private val users = ConcurrentHashMap<UUID, User>()
    private val online = ConcurrentHashMap.newKeySet<UUID>()
    private val quitSaves = ConcurrentHashMap<UUID, Job>()
    private val saveLock = Mutex()
    private val autoSave = scope.launch {
        while (isActive) {
            delay(plugin.cfg.autoSaveInterval.toKotlinDuration())
            saveAll()
        }
    }

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    operator fun get(uuid: UUID): User? = users[uuid]

    operator fun get(player: Player): User = users[player.uniqueId] ?: throw IllegalStateException("${player.name} has no loaded user")

    suspend fun <T> withSaveLock(block: suspend () -> T): T = saveLock.withLock { block() }

    fun shutdown() {
        autoSave.cancel()

        runBlocking {
            withTimeoutOrNull(10.seconds) {
                autoSave.join()
                quitSaves.values.joinAll()
            }
        }

        val snapshots = users.values.map { it.snapshot() }
        if (snapshots.isEmpty()) {
            return
        }

        try {
            runBlocking { saveLock.withLock { repository.saveBlocking(snapshots) } }
            EventLogger.info("users_saved") {
                "count" to snapshots.size
                "reason" to "shutdown"
            }
        } catch (ex: Exception) {
            EventLogger.error("users_save_failed", ex) {
                "count" to snapshots.size
                "reason" to "shutdown"
            }
        }
    }

    private suspend fun saveAll() = saveLock.withLock {
        val snapshots = users.values.map { it.snapshot() }
        if (snapshots.isEmpty()) {
            return@withLock
        }

        try {
            repository.save(snapshots)
            snapshots.forEach { if (it.uuid !in online && !quitSaves.containsKey(it.uuid)) users.remove(it.uuid) }
            EventLogger.debug("users_saved") {
                "count" to snapshots.size
                "reason" to "auto_save"
            }
        } catch (ex: Exception) {
            EventLogger.error("users_save_failed", ex) {
                "count" to snapshots.size
                "reason" to "auto_save"
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return
        }

        val uuid = event.uniqueId

        try {
            runBlocking {
                quitSaves[uuid]?.join()

                if (!users.containsKey(uuid)) {
                    users[uuid] = repository.loadOrCreate(uuid)
                }
            }
        } catch (ex: Exception) {
            EventLogger.error("user_load_failed", ex) { "player_id" to uuid }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, plugin.messages.users.loadFailed())
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onAsyncPlayerPreLoginResult(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED && event.uniqueId !in online) {
            users.remove(event.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        online += uuid

        if (!users.containsKey(uuid)) {
            event.player.kick(plugin.messages.users.loadFailed(), PlayerKickEvent.Cause.PLUGIN)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        online -= uuid

        val snapshot = users[uuid]?.snapshot() ?: return

        quitSaves[uuid] = scope.launch {
            try {
                saveLock.withLock { repository.save(listOf(snapshot)) }

                if (uuid !in online) {
                    users.remove(uuid)
                }
            } catch (ex: Exception) {
                EventLogger.error("user_save_failed", ex) {
                    "player_id" to uuid
                    "reason" to "quit"
                }
            } finally {
                quitSaves.remove(uuid)
            }
        }
    }
}
