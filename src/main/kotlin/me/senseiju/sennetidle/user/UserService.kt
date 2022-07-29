package me.senseiju.sennetidle.user

import me.senseiju.sentils.service.Service
import java.util.*

class UserService : Service() {
    private val userPlaceholderExpansion = UserPlaceholderExpansion()
    private val userCache = UserCache()

    init {
        userPlaceholderExpansion.register()
    }

    fun getUser(playerId: UUID): User {
        return userCache.users[playerId]!!
    }
}