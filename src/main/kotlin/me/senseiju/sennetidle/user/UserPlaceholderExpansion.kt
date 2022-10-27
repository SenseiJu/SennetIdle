package me.senseiju.sennetidle.user

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.utils.extensions.compactDecimalFormat
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

class UserPlaceholderExpansion : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "idle"
    }

    override fun getAuthor(): String {
        return "SenseiJu"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) {
            return null
        }
        val user = userService.getUser(player.uniqueId)

        return when (params.lowercase()) {
            "user_current_wave" -> user.currentWave.toString()
            "user_dps" -> user.dps.compactDecimalFormat()
            "user_promotions" -> user.promotions.toString()
            "user_mob_health" -> {
                val currentHealth = user.idleMob?.currentHealth?.coerceAtLeast(0)?.compactDecimalFormat() ?: "0"
                val maxHealth = user.idleMob?.maxHealth?.compactDecimalFormat() ?: "0"

                return "$currentHealth/$maxHealth"
            }
            "user_cps" -> user.idleMob?.cps?.toString() ?: "0"
            else -> null
        }
    }
}