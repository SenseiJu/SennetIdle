package me.senseiju.sennetidle.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.senseiju.sentils.storage.ConfigFile
import org.bukkit.Keyed
import java.security.Key
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetProvider

class Database(config: ConfigFile) {
    private val source: HikariDataSource
    private val rowSetFactory = RowSetProvider.newFactory()

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = buildJdbcUrl(
            config.getString("host", "localhost"),
            config.getInt("port", 3306),
            config.getString("database", "{NO_DATABASE_SPECIFIED}")
        )
        hikariConfig.username = config.getString("username", "root")
        hikariConfig.password = config.getString("password", "root")
        hikariConfig.connectionTimeout = 8000
        hikariConfig.maximumPoolSize = 10
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        source = HikariDataSource(hikariConfig)
    }

    fun update(query: String, replacements: KeyedReplacements) {
        update(query, *replacements.asTypedArray())
    }

    fun update(query: String, vararg replacements: Any = emptyArray()) {
        source.connection.use { conn ->
            val s = conn.prepareStatement(query)

            var i = 1

            replacements.forEach { replacement -> s.setObject(i++, replacement) }

            try {
                s.executeUpdate()
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }
    }

    fun query(query: String, vararg replacements: Any): CachedRowSet {
        source.connection.use { conn ->
            val s = conn.prepareStatement(query)

            var i = 1

            replacements.forEach { replacement -> s.setObject(i++, replacement) }

            val set = try {
                s.executeQuery()
            } catch (ex: SQLException) {
                ex.printStackTrace()
                null
            }

            return cachedSet(set)
        }
    }

    suspend fun asyncUpdate(query: String, replacements: KeyedReplacements) {
        asyncUpdate(query, *replacements.asTypedArray())
    }

    suspend fun asyncUpdate(query: String, vararg replacements: Any = emptyArray()) {
        withContext(Dispatchers.IO) {
            update(query, *replacements)
        }
    }

    suspend fun asyncQuery(query: String, vararg replacements: Any = emptyArray()): CachedRowSet {
        return withContext(Dispatchers.IO) {
            query(query, *replacements)
        }
    }

    fun updateBatch(query: String, replacements: List<KeyedReplacements>) {
        updateBatch(query, *replacements.toTypedArray())
    }

    fun updateBatch(query: String, vararg replacements: KeyedReplacements = emptyArray()) {
        updateBatch(query, *replacements.map { Replacements(*it.keyReplacements, *it.valueReplacements, *it.valueReplacements) }.toTypedArray())
    }

    fun updateBatch(query: String, vararg replacements: Replacements = emptyArray()) {
        source.connection.use { conn ->
            conn.autoCommit = false

            val s = conn.prepareStatement(query)

            var i = 1

            replacements.forEach { set ->
                set.replacements.forEach { replacement ->
                    s.setObject(i++, replacement)
                }

                s.addBatch()

                i = 1
            }

            try {
                s.executeBatch()
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }

            conn.commit()
            conn.autoCommit = true
        }
    }

    private fun cachedSet(set: ResultSet? = null): CachedRowSet {
        return with (rowSetFactory.createCachedRowSet()) {
            if (set != null) {
                this.populate(set)
            }

            this
        }
    }

    private fun buildJdbcUrl(host: String, port: Int, database: String): String {
        return "jdbc:mysql://$host:$port/$database" +
                "?autoReconnect=true&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false"
    }
}

class Replacements(vararg val replacements: Any = emptyArray())

class KeyedReplacements(val keyReplacements: Array<Any>, vararg val valueReplacements: Any = emptyArray()) {
    fun asTypedArray() =  arrayOf(*keyReplacements, *valueReplacements, *valueReplacements)
}