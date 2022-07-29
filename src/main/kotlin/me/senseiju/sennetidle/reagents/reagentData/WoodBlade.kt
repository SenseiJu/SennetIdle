package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent

class WoodBlade : CraftableReagent {
    override val reagentRequirements = mapOf(Reagent.WOOD to 5)
}