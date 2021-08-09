package me.senseiju.sennetidle

import com.zaxxer.hikari.HikariDataSource
import me.senseiju.sennetidle.database.PlayerRegentsTable
import me.senseiju.sentils.service.ServiceProvider
import me.senseiju.sentils.storage.ConfigFile
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

val serviceProvider = ServiceProvider()

class SennetIdle : JavaPlugin() {

    override fun onEnable() {
        establishDatabase()
    }

    override fun onDisable() {
        serviceProvider.disableAll()
    }

    /**
     * Creates connection to database and relevant tables
     */
    private fun establishDatabase() {
        val databaseConfig = ConfigFile(this, "database.yml", true)
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = databaseConfig.getString("url", "jdbc:mysql://localhost/sennetidle")
        dataSource.username = databaseConfig.getString("username", "root")
        dataSource.password = databaseConfig.getString("password", "toor")
        dataSource.maximumPoolSize = 10
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(PlayerRegentsTable)
        }
    }
}