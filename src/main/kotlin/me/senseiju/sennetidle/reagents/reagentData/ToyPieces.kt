package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object ToyPieces : CraftableReagent {
    override val promotionUnlock = 3
    override val waveUnlock = 151
    override val name = "Toy Pieces"
    override val material = Material.ARMOR_STAND
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.WOOD to 1, Reagent.GOLD to 1)
    override val amountPerCraft = 1
}