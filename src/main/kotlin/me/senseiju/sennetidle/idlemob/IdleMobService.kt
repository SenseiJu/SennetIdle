package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.user.User
import me.senseiju.sentils.service.Service

class IdleMobService : Service() {
    private val idleMobSpawnHandler = IdleMobSpawnHandler()

    init {
        IdleMobPlayerInvisibilityHandler()
        IdleMobParticlePacketListener()
        IdleMobHolograms()
    }

    override fun onDisable() {
        idleMobSpawnHandler.disposeAllMobs()
    }

    fun forceNewIdleMob(user: User) {
        idleMobSpawnHandler.spawnNewIdleMob(user)
    }
}