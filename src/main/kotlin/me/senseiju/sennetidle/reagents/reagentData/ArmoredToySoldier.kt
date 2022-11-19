package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object ArmoredToySoldier : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 6
    override val waveUnlock = 331
    override val name = "Armored Toy Soldier"
    override val material = Material.ARMOR_STAND
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.TOY_SOLDIER to 1, Reagent.NEBULA to 1, Reagent.DIAMOND_SWORD to 1, Reagent.IRON to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}