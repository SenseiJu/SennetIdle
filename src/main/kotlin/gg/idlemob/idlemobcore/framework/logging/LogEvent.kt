package gg.idlemob.idlemobcore.framework.logging

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.bukkit.OfflinePlayer
import java.time.Instant
import java.util.*

class LogEvent(event: String, player: OfflinePlayer? = null, debug: Boolean = false) {
    private val json = JsonObject()

    init {
        json.addProperty("event", event)
        json.addProperty("timestamp", Instant.now().toString())

        if (debug) {
            json.addProperty("debug", true)
        }

        if (player != null) {
            json.addProperty("player_id", player.uniqueId.toString())
            player.name?.let { json.addProperty("player_name", it) }
        }
    }

    infix fun String.to(value: Any?) {
        json.add(this, value.toJson())
    }

    override fun toString(): String = json.toString()

    private fun Any?.toJson(): JsonElement = when (this) {
        null -> JsonNull.INSTANCE
        is JsonElement -> this
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Enum<*> -> JsonPrimitive(name)
        is UUID -> JsonPrimitive(toString())
        is Iterable<*> -> JsonArray().also { array -> forEach { array.add(it.toJson()) } }
        is Array<*> -> JsonArray().also { array -> forEach { array.add(it.toJson()) } }
        else -> JsonPrimitive(toString())
    }
}
