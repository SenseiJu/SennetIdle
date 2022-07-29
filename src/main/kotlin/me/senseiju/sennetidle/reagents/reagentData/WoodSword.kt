package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class WoodSword : CraftableReagent, DamagingReagent {
    override val reagentRequirements = mapOf(Reagent.WOOD_BLADE to 1, Reagent.WOOD_HANDLE to 1)
    override val damagePerSecond = 15
}