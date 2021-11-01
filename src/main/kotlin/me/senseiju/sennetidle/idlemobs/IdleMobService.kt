package me.senseiju.sennetidle.idlemobs

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.users.UserService
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.runnables.newRunnable
import me.senseiju.sentils.service.Service
import me.senseiju.sentils.storage.ConfigFile
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.log

private val userService = serviceProvider.get<UserService>()

class IdleMobService(private val plugin: SennetIdle) : Service(), Listener {
    private val mobsConfig = ConfigFile(plugin, "mobs.yml", true)
    private var spawnLocation: Location
    private val bossWaveInterval: Int
    private val bossWaveTimeLimit: Int

    private val userWaveHandlers = hashMapOf<UUID, WaveHandler>()

    init {
        spawnLocation = mobsConfig.getLocation("spawnLocation") ?: throw Exception("Invalid mob spawn location set")
        bossWaveInterval = mobsConfig.getInt("bossWaveInterval", 5)
        bossWaveTimeLimit = mobsConfig.getInt("bossWaveTimeLimit", 30)

        IdleMobParticleListener(plugin)

        plugin.registerEvents(this)
    }

    fun disposeWaveHandler(uuid: UUID) {
        userWaveHandlers.remove(uuid)?.dispose()
    }

    fun accelerateWaveHandlers(user: User) {
        val logOutPeriod = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - user.lastSeen)

        newRunnable {
            for (i in 0..logOutPeriod) {
                userWaveHandlers[user.uuid]?.run()
            }
        }.runTaskAsynchronously(plugin)
    }

    private fun createWaveHandler(player: Player) {
        val user = userService.getUser(player.uniqueId)

        userWaveHandlers[player.uniqueId] = WaveHandler(player, user, spawnLocation, bossWaveInterval, bossWaveTimeLimit)
    }

    override fun onReload() {
        mobsConfig.reload()

        spawnLocation = mobsConfig.getLocation("spawnLocation") ?: throw Exception("Invalid mob spawn location set")
    }

    override fun onDisable() {
        userWaveHandlers.values.forEach { it.dispose() }
    }

    @EventHandler
    private fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        if (!e.player.isOnline) return
        if (userWaveHandlers.containsKey(e.player.uniqueId)) return

        createWaveHandler(e.player)
    }
}