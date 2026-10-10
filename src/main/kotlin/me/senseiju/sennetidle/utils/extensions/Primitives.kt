package me.senseiju.sennetidle.utils.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.text.NumberFormat
import java.util.*

private val miniMessage = MiniMessage.miniMessage()

fun Number.asCurrencyFormat(): String {
    return "%,d".format(Locale.US, toLong())
}

/**
 * Parses MiniMessage, with italics disabled so item names and lore don't render italic
 */
fun String.component(): Component {
    return miniMessage.deserialize(this).decoration(TextDecoration.ITALIC, false)
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
