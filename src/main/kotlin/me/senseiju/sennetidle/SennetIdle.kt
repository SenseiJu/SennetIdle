package me.senseiju.sennetidle

import com.zaxxer.hikari.HikariDataSource
import me.mattstudios.mf.base.CommandManager
import me.senseiju.sennetidle.commands.SennetIdleCommand
import me.senseiju.sennetidle.regents.RegentService
import me.senseiju.sennetidle.users.UserService
import me.senseiju.sentils.service.ServiceProvider
import me.senseiju.sentils.storage.ConfigFile
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.Style
import org.bukkit.plugin.java.JavaPlugin
import org.ktorm.database.Database
import org.ktorm.support.mysql.MySqlDialect

val serviceProvider = ServiceProvider()

lateinit var database: Database
    private set

class SennetIdle : JavaPlugin() {
    private lateinit var commandManager: CommandManager

    override fun onEnable() {
        enableServices()
        registerCommands()
        establishDatabase()
    }

    override fun onDisable() {
        serviceProvider.disableAll()

        server.onlinePlayers.forEach {
            it.kick(text("SennetIdle is restarting...", Style.empty()))
        }
    }

    private fun enableServices() {
        serviceProvider.add(RegentService(this))
        serviceProvider.add(UserService(this))
    }

    private fun registerCommands() {
        commandManager = CommandManager(this)

        commandManager.register(
            SennetIdleCommand(this)
        )
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
        dataSource.addDataSourceProperty("cachePrepStmts", "true")
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250")
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        database = Database.connect(dataSource, MySqlDialect())
    }
}