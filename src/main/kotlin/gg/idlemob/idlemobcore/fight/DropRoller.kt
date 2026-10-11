package gg.idlemob.idlemobcore.fight

import gg.idlemob.idlemobcore.resource.ResourceRegistry
import gg.idlemob.idlemobcore.resource.isUnlockedFor
import gg.idlemob.idlemobcore.stats.Stat
import gg.idlemob.idlemobcore.stats.Stats
import gg.idlemob.idlemobcore.user.User
import kotlin.random.Random

class DropRoller(private val resources: ResourceRegistry) {

    fun roll(user: User, stats: Stats, boss: Boolean): Map<String, Double> {
        val drops = HashMap<String, Double>()

        resources.entries.forEach { (id, resource) ->
            val droppable = resource.components.droppable ?: return@forEach
            val drop = (if (boss) droppable.boss else droppable.mob) ?: return@forEach

            if (!resource.isUnlockedFor(user)) {
                return@forEach
            }

            if (Random.nextDouble(100.0) >= drop.chance * stats[Stat.DROP_CHANCE]) {
                return@forEach
            }

            val stagesPastUnlock = (user.stage - resource.unlock.stage).coerceAtLeast(0)
            val stageMultiplier = 1 + drop.stageBonus / 100 * stagesPastUnlock
            val amount = Random.nextInt(drop.amount.min, drop.amount.max + 1) * stageMultiplier * stats[Stat.DROP_AMOUNT]

            if (amount > 0) {
                drops[id] = amount
            }
        }

        return drops
    }
}
