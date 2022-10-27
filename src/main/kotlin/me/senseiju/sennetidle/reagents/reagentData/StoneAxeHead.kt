package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object StoneAxeHead : CraftableReagent {
    override val promotionUnlock = 0
    override val name = "Stone Axe Head"
    override val material = Material.STONE_AXE
    override val modelData = 100
    override val reagentRequirements = mapOf(Reagent.STONE to 6)
    override val amountPerCraft = 1
}