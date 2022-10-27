package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object WoodenBlade : CraftableReagent {
    override val promotionUnlock = 0
    override val name = "Wooden Blade"
    override val material = Material.WOODEN_SWORD
    override val modelData = 200
    override val reagentRequirements = mapOf(Reagent.WOOD to 4)
    override val amountPerCraft = 1
}