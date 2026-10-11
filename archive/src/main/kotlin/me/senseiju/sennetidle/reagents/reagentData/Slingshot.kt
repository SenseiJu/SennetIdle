package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object Slingshot : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 0
    override val waveUnlock = 31
    override val name = "Slingshot"
    override val material = Material.BOW
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.WOOD to 1, Reagent.STRING to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}