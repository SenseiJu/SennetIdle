package me.senseiju.sennetidle.utils.extensions

import me.senseiju.sentils.extensions.primitives.asCurrencyFormat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

private val miniMessage = MiniMessage.miniMessage()

fun Int.asCurrencyFormat(prefix: String? = null, suffix: String? = null): String {
    return toLong().asCurrencyFormat(prefix, suffix)
}

fun String.component(): Component {
    return miniMessage.deserialize(this)
}