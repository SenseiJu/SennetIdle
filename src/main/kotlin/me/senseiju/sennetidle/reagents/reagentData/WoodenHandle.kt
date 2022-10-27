package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object WoodenHandle : CraftableReagent {
    override val promotionUnlock = 0
    override val name = "Wooden Handle"
    override val material = Material.WOODEN_SWORD
    override val modelData = 100
    override val reagentRequirements = mapOf(Reagent.WOOD to 2)
    override val amountPerCraft = 3
}