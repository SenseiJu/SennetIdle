package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Wood : DroppableReagent {
    override val promotionUnlock = 0
    override val name = "Wood"
    override val material = Material.OAK_PLANKS
    override val modelData = 0
    override val waveUnlock = 1
    override val dropChance = 100F
    override val minAmount = 2
    override val maxAmount= 5
}