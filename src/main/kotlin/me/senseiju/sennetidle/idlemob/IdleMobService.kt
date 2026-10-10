package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.Service

class IdleMobService : Service() {
    private val idleMobSpawnHandler = IdleMobSpawnHandler()
    private val idleMobHolograms = IdleMobHolograms()

    init {
        IdleMobPlayerInvisibilityHandler()
    }

    override fun onDisable() {
        idleMobSpawnHandler.disposeAllMobs()
        idleMobHolograms.dispose()
    }

    fun forceNewIdleMob(user: User) {
        idleMobSpawnHandler.spawnNewIdleMob(user)
    }
}
