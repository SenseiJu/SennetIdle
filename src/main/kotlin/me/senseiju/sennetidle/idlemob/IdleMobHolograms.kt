package me.senseiju.sennetidle.idlemob

import eu.decentsoftware.holograms.api.DHAPI
import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.extensions.color
import me.senseiju.sentils.extensions.parseLocation
import org.bukkit.Location

class IdleMobHolograms {
    private val hologramLocation = plugin.config.getStringList("idle-mob.hologram-locations").map { it.parseLocation() }
    private val hologramLines = listOf(
        "&b&lCurrent Wave: &e%idle_user_current_wave%",
        "",
        "&b&lHealth: &e%idle_user_mob_health%",
        "&b&lCPS: &e%idle_user_cps%",
        "&b&lDPS: &e%idle_user_dps%"
    ).color()
    private val holograms = hologramLocation.mapIndexed { index: Int, location: Location ->
        DHAPI.createHologram("player_stats_$index", location, hologramLines)
    }

    init {
        holograms.forEach { it.updateInterval = 1 }
    }
}