package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.user.User
import me.senseiju.sentils.service.Service

class IdleMobService : Service() {
    private val idleMobSpawnHandler = IdleMobSpawnHandler()

    init {
        IdleMobPlayerInvisibilityHandler()
        IdleMobHolograms()
        IdleMobParticlePacketListener()
    }

    override fun onDisable() {
        idleMobSpawnHandler.disposeAllMobs()
    }

    fun getIdleMob(user: User): IdleMob? {
        return idleMobSpawnHandler.idleMobs[user.idleMobId]
    }
}