package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object DiamondSword : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 5
    override val waveUnlock = 251
    override val name = "Diamond Sword"
    override val material = Material.DIAMOND_SWORD
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.DIAMOND_BLADE to 1, Reagent.WOODEN_HANDLE to 5, Reagent.IRON_SWORD to 9)
    override val damagePerSecond = 73695
    override val bossOnly = false
    override val amountPerCraft = 1
}