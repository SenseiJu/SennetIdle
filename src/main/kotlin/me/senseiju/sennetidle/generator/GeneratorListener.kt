package me.senseiju.sennetidle.generator

import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class GeneratorListener(
    private val generatorService: GeneratorService
) : Listener {
    private val userService = serviceProvider.get<UserService>()

    @EventHandler
    private fun onGeneratorRightClick(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_BLOCK) return

        val generator = generatorService.getGeneratorByLocation((e.clickedBlock ?: return).location) ?: return
        val user = userService.getUser(e.player.uniqueId)

        e.player.openGeneratorGui(user, generator)

        e.isCancelled = true
    }
}