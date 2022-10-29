package me.senseiju.sennetidle.storage

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.storage.implementation.StorageImplementation
import me.senseiju.sennetidle.storage.implementation.sql.SqlStorage
import me.senseiju.sennetidle.user.User
import me.senseiju.sentils.service.Service
import me.senseiju.sentils.storage.ConfigFile
import java.util.*

class StorageService : Service() {
    private val storageImplementation: StorageImplementation

    init {
        val dbConfig = ConfigFile(plugin, "database.yml", true)

        storageImplementation = SqlStorage(
            dbConfig.getString("host", "localhost:3306"),
            dbConfig.getString("username", "root"),
            dbConfig.getString("password", "toor"),
            dbConfig.getString("database", "sennetidle")
        )
    }

    fun loadUser(playerId: UUID): User {
        return storageImplementation.loadUser(playerId)
    }

    fun saveUser(user: User) {
        storageImplementation.saveUser(user)
    }
}