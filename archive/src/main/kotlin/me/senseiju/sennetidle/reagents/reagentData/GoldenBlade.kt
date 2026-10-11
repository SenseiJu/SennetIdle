package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object GoldenBlade : CraftableReagent {
    override val promotionUnlock = 2
    override val waveUnlock = 101
    override val name = "Golden Blade"
    override val material = Material.GOLDEN_SWORD
    override val modelData = 200
    override val reagentRequirements = mapOf(Reagent.IRON to 5)
    override val amountPerCraft = 1
}