package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object Bow : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 1
    override val waveUnlock = 81
    override val name = "Bow"
    override val material = Material.BOW
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.SLINGSHOT to 1, Reagent.STONE to 1, Reagent.STRING to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}