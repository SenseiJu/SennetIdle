package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Iron : DroppableReagent {
    override val promotionUnlock = 4
    override val waveUnlock = 201
    override val name = "Iron"
    override val material = Material.IRON_INGOT
    override val modelData = 0
    override val dropChance = 85F
    override val minAmount = 2
    override val maxAmount= 5
}