package me.senseiju.sennetidle.idlemobs

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import me.senseiju.sennetidle.SennetIdle

private val protocolManager = ProtocolLibrary.getProtocolManager()

private val particleIds = setOf(0, 5)

class IdleMobParticleListener(
    plugin: SennetIdle
) : PacketAdapter(plugin, PacketType.Play.Server.WORLD_PARTICLES) {

    init {
        protocolManager.addPacketListener(this)
    }

    override fun onPacketSending(event: PacketEvent) {
        if (event.packet.integers.values.any { id ->  particleIds.contains(id)}) {
            event.isCancelled = true
        }
    }
}