package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Diamond : DroppableReagent {
    override val promotionUnlock = 5
    override val waveUnlock = 251
    override val name = "Diamond"
    override val material = Material.DIAMOND
    override val modelData = 0
    override val dropChance = 85F
    override val minAmount = 2
    override val maxAmount= 5
}