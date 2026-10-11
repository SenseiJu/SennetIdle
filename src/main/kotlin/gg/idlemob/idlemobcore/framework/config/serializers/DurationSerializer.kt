package gg.idlemob.idlemobcore.framework.config.serializers

import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.time.Duration
import java.util.function.Predicate

object DurationSerializer : ScalarSerializer<Duration>(Duration::class.java) {
    private val part = Regex("(\\d+)([smhd])", RegexOption.IGNORE_CASE)

    override fun deserialize(type: Type, obj: Any): Duration {
        val text = obj.toString().trim().replace(" ", "")
        if (text.isEmpty() || part.replace(text, "").isNotEmpty()) {
            throw SerializationException("invalid duration '$obj', use e.g. 30s, 10m, 2h or 1h30m")
        }

        return part.findAll(text).fold(Duration.ZERO) { total, match ->
            val amount = match.groupValues[1].toLong()
            total + when (match.groupValues[2].lowercase()) {
                "s" -> Duration.ofSeconds(amount)
                "m" -> Duration.ofMinutes(amount)
                "h" -> Duration.ofHours(amount)
                else -> Duration.ofDays(amount)
            }
        }
    }

    override fun serialize(item: Duration, typeSupported: Predicate<Class<*>>): Any {
        if (item.isZero) {
            return "0s"
        }

        return buildString {
            item.toDaysPart().takeIf { it > 0 }?.let { append("${it}d") }
            item.toHoursPart().takeIf { it > 0 }?.let { append("${it}h") }
            item.toMinutesPart().takeIf { it > 0 }?.let { append("${it}m") }
            item.toSecondsPart().takeIf { it > 0 }?.let { append("${it}s") }
        }
    }
}
