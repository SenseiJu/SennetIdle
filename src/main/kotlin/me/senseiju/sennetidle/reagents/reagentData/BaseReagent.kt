package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import java.util.*

private val random = Random()

interface BaseReagent

interface DroppableReagent : BaseReagent {
    val waveUnlock: Int
    val dropChance: Float
    val minAmount: Int
    val maxAmount: Int

    fun shouldDrop(): Boolean {
        return random.nextFloat(0F, 100F) <= dropChance
    }

    fun randomAmount(): Int {
        return random.nextInt(minAmount, maxAmount + 1)
    }
}

interface CraftableReagent : BaseReagent {
    val reagentRequirements: Map<Reagent, Int>
}

interface DamagingReagent : BaseReagent {
    val damagePerSecond: Int
}