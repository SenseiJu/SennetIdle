package me.senseiju.sennetidle.utils.extensions

import me.senseiju.sentils.extensions.primitives.asCurrencyFormat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.text.NumberFormat
import java.util.*

private val miniMessage = MiniMessage.miniMessage()

fun Int.asCurrencyFormat(prefix: String? = null, suffix: String? = null): String {
    return toLong().asCurrencyFormat(prefix, suffix)
}

fun String.component(): Component {
    return miniMessage.deserialize(this)
}

private val compactNumberFormatter = ThreadLocal.withInitial {
    NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)
        .apply {
            minimumFractionDigits = 2
        }
}
fun Number.compactDecimalFormat(): String {
    return compactNumberFormatter.get().format(this)
}