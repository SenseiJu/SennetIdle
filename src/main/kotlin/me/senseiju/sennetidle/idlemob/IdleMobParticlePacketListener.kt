package me.senseiju.sennetidle.idlemob

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import me.senseiju.sennetidle.plugin

private val protocolManager = ProtocolLibrary.getProtocolManager()

private val particleIds = setOf(8, 10, 12, 20, 40, 50, 250)

class IdleMobParticlePacketListener : PacketAdapter(plugin, PacketType.Play.Server.WORLD_PARTICLES) {

    init {
        protocolManager.addPacketListener(this)
    }

    override fun onPacketSending(event: PacketEvent) {
        if (event.packet.integers.values.any { id -> particleIds.contains(id)}) {
            event.isCancelled = true
        }
    }
}