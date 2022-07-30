package me.senseiju.sennetidle.storage.implementation.sql

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.storage.implementation.StorageImplementation
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.extensions.updateBatchQuery
import me.senseiju.sentils.storage.ConfigFile
import me.senseiju.sentils.storage.Database
import me.senseiju.sentils.storage.Replacements
import java.util.EnumMap
import java.util.UUID

private const val USER_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `users` (" +
        "`player_id` VARCHAR(36) NOT NULL, " +
        "`current_wave` INT NOT NULL DEFAULT 1, " +
        "PRIMARY KEY (`player_id`));"
private const val REAGENT_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `reagents` (" +
        "`player_id` VARCHAR(36) NOT NULL, " +
        "`reagent` VARCHAR(64) NOT NULL, " +
        "`amount` INT NOT NULL DEFAULT 0, " +
        "PRIMARY KEY (`player_id`, `reagent`));"

private const val USER_SELECT = "SELECT * FROM `users` WHERE `player_id`=?;"
private const val USER_INSERT = "INSERT INTO `users` (`player_id`, `current_wave`) VALUES (?,?);"
private const val USER_UPDATE = "UPDATE `users` SET `current_wave`=? WHERE `player_id`=?;"

private const val REAGENT_SELECT = "SELECT * FROM `reagents` WHERE `player_id`=?;"
private const val REAGENT_DELETE = "DELETE FROM `reagents` WHERE `player_id`=?;"
private const val REAGENT_INSERT = "INSERT INTO `reagents` (`player_id`, `reagent`, `amount`) VALUES (?,?,?);"

class SqlStorage : StorageImplementation {
    private val database = Database(ConfigFile(plugin, "database.yml", true))

    init {
        generateSchema()
    }

    private fun generateSchema() {
        database.updateQuery(USER_CREATE_TABLE)
        database.updateQuery(REAGENT_CREATE_TABLE)
    }

    override fun loadUser(playerId: UUID): User {
        return database.query(USER_SELECT, playerId.toString()).use { set ->
            if (!set.next()) {
                User.new(playerId).let {
                    insertUser(it)

                    it
                }
            } else {
                User(
                    playerId,
                    set.getInt("current_wave"),
                    loadUserReagents(playerId)
                )
            }
        }
    }

    private fun loadUserReagents(playerId: UUID): EnumMap<Reagent, Int> {
        return database.query(REAGENT_SELECT, playerId.toString()).use {
            val map = Reagent.emptyUserMap()
            while (it.next()) {
                val reagent = try {
                    Reagent.valueOf(it.getString("reagent"))
                } catch (e: IllegalArgumentException) {
                    plugin.slF4JLogger.error("${it.getString("reagent")} reagent does not exist")
                    continue
                }

                map[reagent] = it.getInt("amount")
            }

            map
        }
    }

    override fun saveUser(user: User) {
        database.updateQuery(USER_UPDATE, user.currentWave, user.playerId.toString())

        saveUserReagents(user)
    }

    private fun saveUserReagents(user: User) {
        database.updateQuery(REAGENT_DELETE, user.playerId.toString())
        database.updateBatchQuery(
            REAGENT_INSERT,
            user.reagents
                .map {
                    Replacements(user.playerId.toString(), it.key.toString(), it.value)
                }
        )
    }

    private fun insertUser(user: User) {
        database.updateQuery(USER_INSERT, user.playerId.toString(), user.currentWave)
    }
}