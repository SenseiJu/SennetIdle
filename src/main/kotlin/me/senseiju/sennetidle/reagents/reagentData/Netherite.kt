package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object Netherite : DroppableReagent {
    override val promotionUnlock = 7
    override val waveUnlock = 201
    override val name = "Netherite"
    override val material = Material.NETHERITE_INGOT
    override val modelData = 0
    override val dropChance = 85F
    override val minAmount = 2
    override val maxAmount= 5
}