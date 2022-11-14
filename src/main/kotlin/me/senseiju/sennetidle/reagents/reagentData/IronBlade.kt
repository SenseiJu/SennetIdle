package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object IronBlade : CraftableReagent {
    override val promotionUnlock = 4
    override val waveUnlock = 201
    override val name = "Iron Blade"
    override val material = Material.IRON_SWORD
    override val modelData = 200
    override val reagentRequirements = mapOf(Reagent.IRON to 5)
    override val amountPerCraft = 1
}