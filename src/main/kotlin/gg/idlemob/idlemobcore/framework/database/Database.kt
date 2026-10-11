package gg.idlemob.idlemobcore.framework.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

private const val DRIVER = "org.mariadb.jdbc.Driver"

class Database(private val config: DatabaseConfig) : AutoCloseable {
    private val dataSource: HikariDataSource

    init {
        createDatabaseIfMissing()

        dataSource = HikariDataSource(HikariConfig().apply {
            poolName = "IdleMobCore"
            driverClassName = DRIVER
            jdbcUrl = "jdbc:mariadb://${config.host}:${config.port}/${config.database}"
            username = config.username
            password = config.password
            maximumPoolSize = config.pool.maximumPoolSize
            minimumIdle = config.pool.minimumIdle
            config.properties.forEach(::addDataSourceProperty)
        })
    }

    private val exposed = ExposedDatabase.connect(dataSource)

    fun <T> transaction(block: JdbcTransaction.() -> T): T {
        return transaction(exposed, statement = block)
    }

    suspend fun <T> query(block: JdbcTransaction.() -> T): T {
        return withContext(Dispatchers.IO) { transaction(block) }
    }

    fun <T> connection(block: (Connection) -> T): T {
        return dataSource.connection.use(block)
    }

    fun migrate() {
        connection { MigrationRunner(it).migrate() }
    }

    override fun close() {
        if (!dataSource.isClosed) {
            dataSource.close()
        }
    }

    private fun createDatabaseIfMissing() {
        try {
            Class.forName(DRIVER)
            DriverManager.getConnection("jdbc:mariadb://${config.host}:${config.port}/", config.username, config.password).use {
                it.createStatement().use { statement ->
                    statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `${config.database}`")
                }
            }
        } catch (ex: SQLException) {
            EventLogger.warn("database_create_failed") {
                "database" to config.database
                "error" to ex.message
            }
        }
    }
}
