package gg.idlemob.idlemobcore.resource

import gg.idlemob.idlemobcore.stats.Stat
import gg.idlemob.idlemobcore.stats.StatContext
import gg.idlemob.idlemobcore.stats.StatModifiers
import gg.idlemob.idlemobcore.stats.StatProvider
import gg.idlemob.idlemobcore.user.User

fun ResourceConfig.isUnlockedFor(user: User): Boolean {
    return user.stage >= unlock.stage && user.prestige >= unlock.prestige
}

class ResourceStatProvider(private val resources: ResourceRegistry) : StatProvider {

    override fun modify(context: StatContext, modifiers: StatModifiers) {
        context.user.resources().forEach { (id, amount) ->
            val damaging = resources[id]?.components?.damaging ?: return@forEach

            val hits = when (damaging.target) {
                DamageTarget.ALL -> true
                DamageTarget.MOBS -> !context.boss
                DamageTarget.BOSSES -> context.boss
            }

            if (hits) {
                modifiers.add(Stat.DPS, damaging.dps * amount)
            }
        }
    }
}
