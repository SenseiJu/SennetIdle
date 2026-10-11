package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object DiamondBlade : CraftableReagent {
    override val promotionUnlock = 5
    override val waveUnlock = 251
    override val name = "Diamond Blade"
    override val material = Material.DIAMOND_SWORD
    override val modelData = 200
    override val reagentRequirements = mapOf(Reagent.DIAMOND to 8)
    override val amountPerCraft = 1
}