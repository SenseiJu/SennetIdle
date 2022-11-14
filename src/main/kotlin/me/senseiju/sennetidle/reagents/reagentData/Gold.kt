package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Gold : DroppableReagent {
    override val promotionUnlock = 2
    override val waveUnlock = 101
    override val name = "Gold"
    override val material = Material.GOLD_INGOT
    override val modelData = 0
    override val dropChance = 90F
    override val minAmount = 2
    override val maxAmount= 5
}