package me.senseiju.sennetidle.user

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.senseiju.sennetidle.idlemob.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()
private val idleMobService = serviceProvider.get<IdleMobService>()

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
        val idleMob = idleMobService.getIdleMob(user)

        return when (params.lowercase()) {
            "user_current_wave" -> user.currentWave.toString()
            "user_dps" -> user.dps.toString()
            "user_mob_health" -> idleMob?.mobHealthString() ?: "0/0"
            "user_cps" -> idleMob?.cps?.toString() ?: "0"
            else -> null
        }
    }
}