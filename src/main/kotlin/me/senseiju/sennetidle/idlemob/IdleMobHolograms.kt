package me.senseiju.sennetidle.idlemob

import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.hologram.Hologram
import me.senseiju.sennetidle.plugin

class IdleMobHolograms {
    private val manager = FancyHologramsPlugin.get().hologramManager

    private val holograms: List<Hologram>

    init {
        val config = plugin.cfg.idleMob

        holograms = config.hologramLocations.mapIndexed { index, location ->
            val name = "sennetidle_player_stats_$index"
            manager.getHologram(name).ifPresent(manager::removeHologram)

            val data = TextHologramData(name, location.toLocation())
                .setText(config.hologramLines)
                .setTextUpdateInterval(config.hologramUpdateInterval)
                .setPersistent(false)

            manager.create(data).also(manager::addHologram)
        }
    }

    fun dispose() {
        holograms.forEach(manager::removeHologram)
    }
}
