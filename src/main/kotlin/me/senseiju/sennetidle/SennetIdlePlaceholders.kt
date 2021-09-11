package me.senseiju.sennetidle

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.senseiju.sennetidle.users.UserService
import org.bukkit.OfflinePlayer

private val userService = serviceProvider.get<UserService>()

class SennetIdlePlaceholders : PlaceholderExpansion() {

    override fun getIdentifier() = "sennetidle"

    override fun getAuthor() = "SenseiJu"

    override fun getVersion() = "0.0.1"

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer, params: String): String? {
        val user = userService.getUser(player.uniqueId)

        return when (params.lowercase()) {
            "passive_dps" -> user.passiveDPS.toString()
            "current_wave" -> user.currentWave.toString()
            else -> null
        }
    }
}