package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Iron : DroppableReagent {
    override val promotionUnlock = 2
    override val name = "Iron"
    override val material = Material.IRON_INGOT
    override val modelData = 0
    override val waveUnlock = 51
    override val dropChance = 90F
    override val minAmount = 2
    override val maxAmount= 5
}