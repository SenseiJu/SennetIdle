package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class IronSword : CraftableReagent, DamagingReagent {
    override val reagentRequirements = mapOf(Reagent.IRON_BLADE to 1, Reagent.WOOD_HANDLE to 3, Reagent.STONE_SWORD to 9)
    override val damagePerSecond = 4335
}