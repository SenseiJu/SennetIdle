package me.senseiju.sennetidle.generator.holograms

import me.senseiju.sennetidle.generator.GeneratorService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

private val userService = serviceProvider.get<UserService>()
private val generatorService = serviceProvider.get<GeneratorService>()

class HologramListener : Listener {

    @EventHandler
    private fun onPlayerJoin(e: PlayerJoinEvent) {
        val user = userService.getUser(e.player.uniqueId)


        generatorService.generators.values.forEach {
            it.userHolograms[user] = generatorService.hologramHandler.createHologram(e.player, it)
        }
    }

    @EventHandler
    private fun onPlayerQuit(e: PlayerQuitEvent) {
        val user = userService.getUser(e.player.uniqueId)

        generatorService.generators.values.forEach {
            it.userHolograms.remove(user)?.delete()
        }
    }
}