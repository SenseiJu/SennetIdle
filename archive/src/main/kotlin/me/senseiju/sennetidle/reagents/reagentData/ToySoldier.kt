package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object ToySoldier : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 3
    override val waveUnlock = 151
    override val name = "Toy Soldier"
    override val material = Material.ARMOR_STAND
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.TOY_PIECES to 1, Reagent.NEBULA to 1, Reagent.STONE_SWORD to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}