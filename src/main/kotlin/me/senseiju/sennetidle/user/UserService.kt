package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.user.promotions.PromotionsCommand
import me.senseiju.sentils.service.Service
import java.util.*

class UserService : Service() {
    private val userPlaceholderExpansion = UserPlaceholderExpansion()
    private val userCache = UserCache()

    init {
        plugin.commandManager.register(PromotionsCommand())

        userPlaceholderExpansion.register()
    }

    override fun onDisable() {
        userCache.saveUsers()
    }

    fun getUser(playerId: UUID): User {
        return userCache.users[playerId]!!
    }
}