package me.senseiju.sennetidle.npcs

import de.oliver.fancynpcs.api.events.NpcInteractEvent
import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.reagents.openReagentGui
import me.senseiju.sennetidle.upgrades.openUpgradeGui
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class NpcInteractListener : Listener {

    @EventHandler
    private fun onNpcInteractEvent(e: NpcInteractEvent) {
        val npcs = plugin.cfg.npcs
        val name = e.npc.data.name

        when (name) {
            in npcs.reagents -> e.player.openReagentGui()
            in npcs.upgrades -> e.player.openUpgradeGui()
        }
    }
}
