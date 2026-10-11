package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object Crossbow : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 7
    override val waveUnlock = 381
    override val name = "Crossbow"
    override val material = Material.BOW
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.COMPOUND_BOW to 1, Reagent.NETHERITE to 1, Reagent.STRING to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}