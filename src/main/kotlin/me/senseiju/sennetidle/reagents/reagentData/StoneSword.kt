package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class StoneSword : CraftableReagent, DamagingReagent {
    override val reagentRequirements = mapOf(Reagent.STONE_BLADE to 1, Reagent.WOOD_HANDLE to 2, Reagent.WOOD_SWORD to 9)
    override val damagePerSecond = 255
}