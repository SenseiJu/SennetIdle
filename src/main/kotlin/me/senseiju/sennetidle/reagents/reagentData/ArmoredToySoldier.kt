package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object ArmoredToySoldier : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 6
    override val waveUnlock = 331
    override val name = "Armored Toy Soldier"
    override val material = Material.ARMOR_STAND
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.IRON_BLADE to 1, Reagent.WOODEN_HANDLE to 3, Reagent.STONE_SWORD to 9)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}