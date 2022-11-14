package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object StoneBlade : CraftableReagent {
    override val promotionUnlock = 1
    override val waveUnlock = 51
    override val name = "Stone Blade"
    override val material = Material.STONE_SWORD
    override val modelData = 200
    override val reagentRequirements = mapOf(Reagent.STONE to 8)
    override val amountPerCraft = 1
}