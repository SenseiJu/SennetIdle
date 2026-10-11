package gg.idlemob.idlemobcore.framework.database

import gg.idlemob.idlemobcore.framework.logging.EventLogger
import java.security.MessageDigest
import java.sql.Connection
import java.sql.SQLException
import java.util.*

private const val MIGRATIONS_TABLE = "migrations"

class MigrationRunner(private val connection: Connection) {

    fun migrate() {
        val applied = readApplied()

        var version = 1
        while (true) {
            val sql = SqlResources.find(resourcePath(version))
            if (sql == null) {
                if (version <= applied.size) {
                    throw SQLException("Database is at migration ${applied.size} but this build only has ${version - 1}, downgrading isn't supported")
                }
                return
            }

            val checksum = checksum(sql)
            val recorded = applied[version]
            if (recorded == null) {
                apply(version, sql, checksum)
            } else if (recorded != checksum) {
                throw SQLException("Migration $version was changed after it was applied, add a new migration instead")
            }

            version++
        }
    }

    private fun readApplied(): Map<Int, String> {
        val exists = connection.metaData.getTables(connection.catalog, null, MIGRATIONS_TABLE, null).use { it.next() }
        if (!exists) {
            return emptyMap()
        }

        return connection.createStatement().use { statement ->
            statement.executeQuery("SELECT `version`, `checksum` FROM `$MIGRATIONS_TABLE`").use { results ->
                buildMap { while (results.next()) put(results.getInt("version"), results.getString("checksum")) }
            }
        }
    }

    private fun apply(version: Int, sql: String, checksum: String) {
        EventLogger.info("database_migration_applying") { "version" to version }

        connection.autoCommit = false
        try {
            connection.createStatement().use { statement ->
                split(sql).forEach(statement::execute)
            }

            connection.prepareStatement("INSERT INTO `$MIGRATIONS_TABLE` (`version`, `checksum`, `applied_at`) VALUES (?, ?, ?)").use {
                it.setInt(1, version)
                it.setString(2, checksum)
                it.setLong(3, System.currentTimeMillis())
                it.executeUpdate()
            }

            connection.commit()
        } catch (ex: SQLException) {
            connection.rollback()
            throw SQLException("Migration $version failed and was rolled back", ex)
        } finally {
            connection.autoCommit = true
        }
    }

    private fun resourcePath(version: Int) = "database/migrations/$version.sql"

    private fun split(sql: String): List<String> {
        return sql.lines()
            .filterNot { it.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun checksum(sql: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(sql.replace("\r\n", "\n").toByteArray())
        return HexFormat.of().formatHex(digest)
    }
}
