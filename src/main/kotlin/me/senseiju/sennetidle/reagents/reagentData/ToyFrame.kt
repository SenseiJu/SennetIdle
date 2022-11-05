package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object ToyFrame : CraftableReagent {
    override val promotionUnlock = 0
    override val name = "Toy Frame"
    override val material = Material.ARMOR_STAND
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.IRON_BLADE to 1, Reagent.WOODEN_HANDLE to 3, Reagent.STONE_SWORD to 9)
    override val amountPerCraft = 1
}