package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.service.Service
import java.util.*

class UserService : Service() {
    private val userPlaceholderExpansion = UserPlaceholderExpansion()
    private val userCache = UserCache()

    init {
        plugin.commandManager.register(PromoteCommand())

        userPlaceholderExpansion.register()
    }

    override fun onDisable() {
        userCache.saveUsers()
    }

    fun getUser(playerId: UUID): User {
        return userCache.users[playerId]!!
    }
}