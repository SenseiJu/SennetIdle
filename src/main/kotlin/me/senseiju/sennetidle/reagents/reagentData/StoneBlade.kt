package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class StoneBlade : CraftableReagent {
    override val reagentRequirements = mapOf(Reagent.STONE to 8)
}