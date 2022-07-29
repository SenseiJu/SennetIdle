package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class DiamondBlade : CraftableReagent {
    override val reagentRequirements = mapOf(Reagent.DIAMOND to 8)
}