package me.senseiju.sennetidle.storage

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.storage.implementation.StorageImplementation
import me.senseiju.sennetidle.storage.implementation.sql.SqlStorage
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.Service
import java.util.*

class StorageService : Service() {
    private val storageImplementation: StorageImplementation = SqlStorage(plugin.cfg.storage)

    override fun onDisable() {
        storageImplementation.close()
    }

    fun loadUser(playerId: UUID): User {
        return storageImplementation.loadUser(playerId)
    }

    fun saveUser(user: User) {
        storageImplementation.saveUser(user)
    }
}
