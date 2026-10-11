package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.utils.ModelItem
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sennetidle.utils.randomChance
import org.bukkit.inventory.ItemStack
import java.util.*

private val random = Random()

interface BaseReagent : ModelItem {
    val promotionUnlock: Int
    val waveUnlock: Int

    override fun itemStack(): ItemStack {
        return super.itemStack().apply {
            editMeta {
                it.displayName("<light_purple><b>$name".component())
            }
        }
    }
}

interface DroppableReagent : BaseReagent {
    val dropChance: Float
    val minAmount: Int
    val maxAmount: Int

    fun shouldDrop(): Boolean {
        return randomChance(dropChance)
    }

    fun randomAmount(): Int {
        return random.nextInt(minAmount, maxAmount + 1)
    }
}

interface CraftableReagent : BaseReagent {
    val reagentRequirements: Map<Reagent, Int>
    val amountPerCraft: Int
}

interface DamagingReagent : BaseReagent {
    val damagePerSecond: Int
    val bossOnly: Boolean
}