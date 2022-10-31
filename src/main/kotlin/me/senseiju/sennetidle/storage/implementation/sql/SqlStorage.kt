package me.senseiju.sennetidle.storage.implementation.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.storage.implementation.StorageImplementation
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.user.User
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.PreparedStatement
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
private const val USER_SELECT =
    """
        SELECT `users`.*, IFNULL(`upgrades_grp`.`upgrade_levels`, '') AS `upgrade_levels`, IFNULL(`reagents_grp`.`reagent_amounts`, '') AS `reagent_amounts`
        FROM `users`
        LEFT JOIN (SELECT `upgrades`.`player_id`, GROUP_CONCAT(`upgrades`.`upgrade`, ':', `upgrades`.`level`) AS `upgrade_levels` FROM `upgrades` WHERE `upgrades`.`player_id` = ? GROUP BY `upgrades`.`player_id`) AS `upgrades_grp` ON `users`.`player_id`= `upgrades_grp`.`player_id`
        LEFT JOIN (SELECT `reagents`.`player_id`, GROUP_CONCAT(`reagents`.`reagent`, ':', `reagents`.`amount`) AS `reagent_amounts` FROM `reagents` WHERE `reagents`.`player_id` = ? GROUP BY `reagents`.`player_id`) AS `reagents_grp` ON `users`.`player_id`= `reagents_grp`.`player_id`
        WHERE `users`.`player_id` = ?
        GROUP BY `users`.`player_id`;
    """
@Language("mysql")
private const val USER_INSERT = "INSERT INTO `users` (`player_id`, `current_wave`, `promotions`, `unspent_upgrade_points`) VALUES (?,?,?,?);"
@Language("mysql")
private const val USER_UPDATE = "UPDATE `users` SET `current_wave`=?, `promotions`=?, `unspent_upgrade_points`=? WHERE `player_id`=?;"

@Language("mysql")
private const val REAGENT_DELETE = "DELETE FROM `reagents` WHERE `player_id`=?;"
@Language("mysql")
private const val REAGENT_INSERT = "INSERT INTO `reagents` (`player_id`, `reagent`, `amount`) VALUES (?,?,?);"

@Language("mysql")
private const val UPGRADE_DELETE = "DELETE FROM `upgrades` WHERE `player_id`=?;"
@Language("mysql")
private const val UPGRADE_INSERT = "INSERT INTO `upgrades` (`player_id`, `upgrade`, `level`) VALUES (?,?,?);"

class SqlStorage(
    host: String,
    username: String,
    password: String,
    database: String
) : StorageImplementation {
    private val dataSource: HikariDataSource

    init {
        dataSource = HikariConfig().apply {
            this.jdbcUrl = "jdbc:mysql://${host}/${database}?autoReconnect=true&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false"
            this.username = username
            this.password = password
            this.connectionTimeout = 8000

            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }.let {
            HikariDataSource(it)
        }
    }

    init {
        generateSchema()
    }

    private fun generateSchema() {
        dataSource.connection.usePreparedStatement(USER_CREATE_TABLE) { it.executeUpdate() }
        dataSource.connection.usePreparedStatement(REAGENT_CREATE_TABLE) { it.executeUpdate() }
        dataSource.connection.usePreparedStatement(UPGRADE_CREATE_TABLE) { it.executeUpdate() }
    }

    override fun loadUser(playerId: UUID): User {
        return dataSource.connection.usePreparedStatement(USER_SELECT) { stmt ->
            repeat(3) {
                stmt.setString(it + 1, playerId.toString())
            }

            val set = stmt.executeQuery()
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
                    convertReagentAmounts(set.getString("reagent_amounts")),
                    convertUpgradeLevels(set.getString("upgrade_levels"))
                )
            }
        }
    }

    private fun convertReagentAmounts(reagentAmounts: String): EnumMap<Reagent, Int> {
        return Reagent.emptyMap().apply {
            reagentAmounts.split(",").forEach {
                val reagent = try {
                    Reagent.valueOf(it.substringBefore(":"))
                } catch (e: IllegalArgumentException) {
                    plugin.slF4JLogger.error("${it.substringBefore(":")} reagent does not exist")
                    return@forEach
                }

                this[reagent] = it.substringAfter(":").toIntOrNull() ?: 0
            }
        }
    }

    private fun convertUpgradeLevels(upgradeLevels: String): EnumMap<Upgrade, Int> {
        return Upgrade.emptyMap().apply {
            upgradeLevels.split(",").forEach {
                val upgrade = try {
                    Upgrade.valueOf(it.substringBefore(":"))
                } catch (e: IllegalArgumentException) {
                    plugin.slF4JLogger.error("${it.substringBefore(":")} upgrade does not exist")
                    return@forEach
                }

                this[upgrade] = it.substringAfter(":").toIntOrNull() ?: 0
            }
        }
    }

    override fun saveUser(user: User) {
        dataSource.connection.usePreparedStatement(USER_UPDATE) { stmt ->
            stmt.setInt(1, user.currentWave)
            stmt.setInt(2, user.promotions)
            stmt.setInt(3, user.unspentUpgradePoints)
            stmt.setString(4, user.playerId.toString())

            stmt.executeUpdate()
        }

        saveUserReagents(user)
        saveUserUpgrades(user)
    }

    private fun saveUserReagents(user: User) {
        dataSource.connection.usePreparedStatement(REAGENT_DELETE) { stmt ->
            stmt.setString(1, user.playerId.toString())

            stmt.executeUpdate()
        }
        dataSource.connection.usePreparedStatement(REAGENT_INSERT) { stmt ->
            user.reagents.forEach {
                stmt.setString(1, user.playerId.toString())
                stmt.setString(2, it.key.toString())
                stmt.setInt(3, it.value)

                stmt.addBatch()
            }

            stmt.executeBatch()
        }
    }

    private fun saveUserUpgrades(user: User) {
        dataSource.connection.usePreparedStatement(UPGRADE_DELETE) { stmt ->
            stmt.setString(1, user.playerId.toString())

            stmt.executeUpdate()
        }
        dataSource.connection.usePreparedStatement(UPGRADE_INSERT) { stmt ->
            user.upgrades.forEach {
                stmt.setString(1, user.playerId.toString())
                stmt.setString(2, it.key.toString())
                stmt.setInt(3, it.value)

                stmt.addBatch()
            }

            stmt.executeBatch()
        }
    }

    private fun insertUser(user: User) {
        dataSource.connection.usePreparedStatement(USER_INSERT) { stmt ->
            stmt.setString(1, user.playerId.toString())
            stmt.setInt(2, user.currentWave)
            stmt.setInt(3, user.promotions)
            stmt.setInt(4, user.unspentUpgradePoints)

            stmt.executeUpdate()
        }
    }

    private fun <R> Connection.usePreparedStatement(query: String, block: (PreparedStatement) -> R): R {
        return use {
            it.prepareStatement(query).use(block)
        }
    }
}