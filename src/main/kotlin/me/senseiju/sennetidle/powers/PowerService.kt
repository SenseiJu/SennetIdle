package me.senseiju.sennetidle.powers

import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PowerService : Service(), Listener {

    init {
        plugin.registerEvents(this)
    }

    @EventHandler
    private fun onRightClickEvent(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_AIR || e.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }


    }
}