package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Stone : DroppableReagent {
    override val promotionUnlock = 1
    override val waveUnlock = 51
    override val name = "Stone"
    override val material = Material.STONE
    override val modelData = 0
    override val dropChance = 95F
    override val minAmount = 2
    override val maxAmount= 5
}