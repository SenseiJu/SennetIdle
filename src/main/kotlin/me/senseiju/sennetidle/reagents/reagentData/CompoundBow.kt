package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object CompoundBow : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 4
    override val waveUnlock = 231
    override val name = "Compound Bow"
    override val material = Material.BOW
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.BOW to 1, Reagent.IRON to 1, Reagent.STRING to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}