package gg.idlemob.idlemobcore.resource

import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.user.User
import org.bukkit.entity.Player
import kotlin.math.floor

class ResourceCrafter(private val resources: ResourceRegistry) {

    fun maxCrafts(user: User, id: String): Int {
        val resource = resources[id] ?: return 0
        val craftable = resource.components.craftable ?: return 0

        if (!resource.isUnlockedFor(user)) {
            return 0
        }

        return craftable.ingredients.minOf { (ingredient, needed) ->
            floor(user.getResource(ingredient) / needed).coerceAtMost(Int.MAX_VALUE.toDouble()).toInt()
        }
    }

    fun craft(player: Player, user: User, id: String, times: Int): Int {
        val craftable = resources[id]?.components?.craftable ?: return 0
        val crafts = times.coerceAtMost(maxCrafts(user, id))
        if (crafts <= 0) {
            return 0
        }

        craftable.ingredients.forEach { (ingredient, needed) ->
            user.addResource(ingredient, -needed.toDouble() * crafts)
        }
        user.addResource(id, craftable.output.toDouble() * crafts)

        EventLogger.debug("resource_crafted", player) {
            "resource" to id
            "crafts" to crafts
        }

        return crafts
    }
}
