package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class DiamondSword : CraftableReagent, DamagingReagent {
    override val reagentRequirements = mapOf(Reagent.DIAMOND_BLADE to 1, Reagent.WOOD_HANDLE to 5, Reagent.IRON_SWORD to 9)
    override val damagePerSecond = 73695
}