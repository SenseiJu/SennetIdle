package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.runnables.newRunnable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class IdleMobPlayerInvisibilityHandler : Listener {
    private val spawnLocation = plugin.config.getLocation("idle-mob.spawn-location") ?: throw Exception("'idle-mob.spawn-location' config option not set!")
    private val playerInvisibilityRadius = plugin.config.getDouble("idle-mob.player-invisibility-radius", 5.0)

    private val playersInRange = hashSetOf<UUID>()

    private val runnable = newRunnable { onTick() }

    init {
        plugin.registerEvents(this)

        runnable.runTaskTimer(plugin, 0L, 4L)
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