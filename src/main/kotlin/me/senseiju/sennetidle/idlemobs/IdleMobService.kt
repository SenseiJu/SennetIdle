package me.senseiju.sennetidle.idlemobs

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.users.UserService
import me.senseiju.sentils.service.Service
import me.senseiju.sentils.storage.ConfigFile
import org.bukkit.Location

class IdleMobService(private val plugin: SennetIdle) : Service() {
    private val mobsConfig = ConfigFile(plugin, "mobs.yml", true)
    private var spawnLocation: Location

    //region Service

    init {
        spawnLocation = mobsConfig.getLocation("spawnLocation") ?: throw Exception("Invalid mob spawn location set")

        IdleMobParticleListener(plugin)
    }

    override fun onReload() {
        mobsConfig.reload()

        spawnLocation = mobsConfig.getLocation("spawnLocation") ?: throw Exception("Invalid mob spawn location set")
    }

    override fun onDisable() {
        serviceProvider.get<UserService>().users.forEach { (_, user) ->
            user.idleMob.dispose()
        }
    }

    //endregion

    fun createIdleMob(user: User): IdleMob {
        return IdleMob(plugin, user, spawnLocation)
    }
}