package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Nebula : DroppableReagent {
    override val promotionUnlock = 3
    override val waveUnlock = 151
    override val name = "Nebula"
    override val material = Material.BLAZE_POWDER
    override val modelData = 0
    override val dropChance = 90F
    override val minAmount = 2
    override val maxAmount= 5
}