package me.senseiju.sennetidle.reagents.reagentData

import org.bukkit.Material

object String : DroppableReagent {
    override val promotionUnlock = 2
    override val name = "String"
    override val material = Material.STRING
    override val modelData = 0
    override val waveUnlock = 61
    override val dropChance = 90F
    override val minAmount = 2
    override val maxAmount= 5
}