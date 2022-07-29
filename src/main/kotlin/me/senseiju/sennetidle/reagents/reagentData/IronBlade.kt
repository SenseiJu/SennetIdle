package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class IronBlade : CraftableReagent {
    override val reagentRequirements = mapOf(Reagent.IRON to 5)
}