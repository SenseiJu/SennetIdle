package me.senseiju.sennetidle.storage.implementation.sql

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.storage.implementation.StorageImplementation
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.extensions.forEach
import me.senseiju.sennetidle.utils.extensions.updateBatchQuery
import me.senseiju.sentils.storage.ConfigFile
import me.senseiju.sentils.storage.Database
import me.senseiju.sentils.storage.Replacements
import org.intellij.lang.annotations.Language
import java.util.*

@Language("mysql")
private const val USER_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `users` (" +
        "`player_id` VARCHAR(36) NOT NULL, " +
        "`current_wave` INT NOT NULL DEFAULT 1, " +
        "`promotions` INT NOT NULL DEFAULT 0," +
        "`unspent_upgrade_points` INT NOT NULL DEFAULT 0," +
        "PRIMARY KEY (`player_id`));"

@Language("mysql")
private const val REAGENT_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `reagents` (" +
        "`player_id` VARCHAR(36) NOT NULL, " +
        "`reagent` VARCHAR(64) NOT NULL, " +
        "`amount` INT NOT NULL DEFAULT 0, " +
        "PRIMARY KEY (`player_id`, `reagent`));"

@Language("mysql")
private const val UPGRADE_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `upgrades` (" +
        "`player_id` VARCHAR(36) NOT NULL, " +
        "`upgrade` VARCHAR(64) NOT NULL, " +
        "`level` INT NOT NULL DEFAULT 0, " +
        "PRIMARY KEY (`player_id`, `upgrade`));"

@Language("mysql")
private const val USER_SELECT = "SELECT * FROM `users` WHERE `player_id`=?;"
@Language("mysql")
private const val USER_INSERT = "INSERT INTO `users` (`player_id`, `current_wave`, `promotions`, `unspent_upgrade_points`) VALUES (?,?,?,?);"
@Language("mysql")
private const val USER_UPDATE = "UPDATE `users` SET `current_wave`=?, `promotions`=?, `unspent_upgrade_points`=? WHERE `player_id`=?;"

@Language("mysql")
private const val REAGENT_SELECT = "SELECT * FROM `reagents` WHERE `player_id`=?;"
@Language("mysql")
private const val REAGENT_DELETE = "DELETE FROM `reagents` WHERE `player_id`=?;"
@Language("mysql")
private const val REAGENT_INSERT = "INSERT INTO `reagents` (`player_id`, `reagent`, `amount`) VALUES (?,?,?);"

@Language("mysql")
private const val UPGRADE_SELECT = "SELECT * FROM `upgrades` WHERE `player_id`=?;"
@Language("mysql")
private const val UPGRADE_DELETE = "DELETE FROM `upgrades` WHERE `player_id`=?;"
@Language("mysql")
private const val UPGRADE_INSERT = "INSERT INTO `upgrades` (`player_id`, `upgrade`, `level`) VALUES (?,?,?);"

class SqlStorage : StorageImplementation {
    private val database = Database(ConfigFile(plugin, "database.yml", true))

    init {
        generateSchema()
    }

    private fun generateSchema() {
        database.updateQuery(USER_CREATE_TABLE)
        database.updateQuery(REAGENT_CREATE_TABLE)
        database.updateQuery(UPGRADE_CREATE_TABLE)
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
                    set.getInt("promotions"),
                    set.getInt("unspent_upgrade_points"),
                    loadUserReagents(playerId),
                    loadUserUpgrades(playerId)
                )
            }
        }
    }

    private fun loadUserReagents(playerId: UUID): EnumMap<Reagent, Int> {
        return database.query(REAGENT_SELECT, playerId.toString()).use {
            val map = Reagent.emptyUserMap()
            it.forEach {
                val reagent = try {
                    Reagent.valueOf(it.getString("reagent"))
                } catch (e: IllegalArgumentException) {
                    plugin.slF4JLogger.error("${it.getString("reagent")} reagent does not exist")
                    return@forEach
                }

                map[reagent] = it.getInt("amount")
            }

            map
        }
    }

    private fun loadUserUpgrades(playerId: UUID): EnumMap<Upgrade, Int> {
        return database.query(UPGRADE_SELECT, playerId.toString()).use {
            val map = Upgrade.emptyUserMap()
            it.forEach {
                val upgrade = try {
                    Upgrade.valueOf(it.getString("upgrade"))
                } catch (e:IllegalArgumentException) {
                    plugin.slF4JLogger.error("${it.getString("upgrade")} upgrade does not exist")
                    return@forEach
                }

                map[upgrade] = it.getInt("level")
            }

            map
        }
    }

    override fun saveUser(user: User) {
        database.updateQuery(USER_UPDATE, user.currentWave, user.promotions, user.unspentUpgradePoints, user.playerId.toString())

        saveUserReagents(user)
        saveUserUpgrades(user)
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

    private fun saveUserUpgrades(user: User) {
        database.updateQuery(UPGRADE_DELETE, user.playerId.toString())
        database.updateBatchQuery(
            UPGRADE_INSERT,
            user.upgrades
                .map {
                    Replacements(user.playerId.toString(), it.key.toString(), it.value)
                }
        )
    }

    private fun insertUser(user: User) {
        database.updateQuery(USER_INSERT, user.playerId.toString(), user.currentWave, user.promotions, user.unspentUpgradePoints)
    }
}