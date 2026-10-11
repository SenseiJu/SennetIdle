package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object MagicCore : CraftableReagent {
    override val promotionUnlock = 5
    override val waveUnlock = 281
    override val name = "Magic Core"
    override val material = Material.GLOWSTONE_DUST
    override val modelData = 200
    override val reagentRequirements = mapOf(Reagent.NETHERITE to 1, Reagent.NEBULA to 1)
    override val amountPerCraft = 1
}