package me.senseiju.sennetidle.powers

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

private val userService = serviceProvider.get<UserService>()

class PowerService : Service(), Listener {

    init {
        println("enabling powers")
        plugin.registerEvents(this)
    }

    @EventHandler
    private fun onRightClickEvent(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) {
            return
        }

        if (e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        Power.values().forEach {
            if (e.player.inventory.heldItemSlot == it.data.slot) {
                it.data.onActivate(userService.getUser(e.player.uniqueId))
                return
            }
        }
        


    }
}