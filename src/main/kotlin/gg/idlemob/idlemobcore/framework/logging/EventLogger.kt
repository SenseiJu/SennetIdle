package gg.idlemob.idlemobcore.framework.logging

import org.bukkit.OfflinePlayer
import org.slf4j.Logger

object EventLogger {
    lateinit var logger: Logger

    var debugEnabled = false

    fun debug(event: String, player: OfflinePlayer? = null, fields: LogEvent.() -> Unit = {}) {
        if (debugEnabled) {
            logger.info(LogEvent(event, player, debug = true).apply(fields).toString())
        }
    }

    fun info(event: String, player: OfflinePlayer? = null, fields: LogEvent.() -> Unit = {}) {
        logger.info(LogEvent(event, player).apply(fields).toString())
    }

    fun warn(event: String, player: OfflinePlayer? = null, fields: LogEvent.() -> Unit = {}) {
        logger.warn(LogEvent(event, player).apply(fields).toString())
    }

    fun error(event: String, error: Throwable, player: OfflinePlayer? = null, fields: LogEvent.() -> Unit = {}) {
        logger.error(LogEvent(event, player).apply(fields).toString(), error)
    }
}
