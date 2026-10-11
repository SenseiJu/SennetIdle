package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.utils.extensions.register
import me.senseiju.sennetidle.utils.extensions.runTaskTimer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class IdleMobPlayerInvisibilityHandler : Listener {
    private val spawnLocation = plugin.cfg.idleMob.spawnLocation.toLocation()
    private val playerInvisibilityRadius = plugin.cfg.idleMob.playerInvisibilityRadius

    private val playersInRange = hashSetOf<UUID>()

    init {
        register()

        runTaskTimer(0L, 4L) { onTick() }
    }

    private fun getNearbyPlayers(): Collection<Player> = spawnLocation.getNearbyPlayers(playerInvisibilityRadius)

    private fun onTick() {
        val newPlayers = getNearbyPlayers()
        playersInRange.filterNot {
            newPlayers.contains(Bukkit.getPlayer(it))
        }.mapNotNull {
            Bukkit.getPlayer(it)
        }.forEach {
            showToPlayers(it)
        }

        newPlayers.filterNot {
            playersInRange.contains(it.uniqueId)
        }.forEach {
            hideFromPlayers(it)
        }

        playersInRange.clear()
        playersInRange.addAll(newPlayers.map { it.uniqueId })
    }

    private fun showToPlayers(player: Player) {
        Bukkit.getOnlinePlayers().forEach {
            it.showPlayer(plugin, player)
        }
    }

    private fun hideFromPlayers(player: Player) {
        Bukkit.getOnlinePlayers().forEach {
            it.hidePlayer(plugin, player)
        }
    }

    @EventHandler
    private fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        if (getNearbyPlayers().contains(e.player)) {
            hideFromPlayers(e.player)

            playersInRange.add(e.player.uniqueId)
        }

        playersInRange.mapNotNull {
            Bukkit.getPlayer(it)
        }.forEach {
            e.player.hidePlayer(plugin, it)
        }
    }
}