package gg.idlemob.idlemobcore.framework.text

import java.awt.Color

private const val LOW_HEALTH_HEX = "#FF3B30"

fun bar(fraction: Double, length: Int, filledColor: String = "<green>"): String {
    val filled = (fraction.coerceIn(0.0, 1.0) * length).toInt()
    return "<dark_gray>[$filledColor${"|".repeat(filled)}<dark_gray>${"|".repeat(length - filled)}]"
}

fun healthBar(fraction: Double, length: Int): String {
    val filled = (fraction.coerceIn(0.0, 1.0) * length).toInt()
    return "<dark_gray>[${healthGradient(fraction, "|".repeat(filled))}<dark_gray>${"|".repeat(length - filled)}]"
}

fun healthGradient(fraction: Double, text: String): String {
    if (text.isEmpty()) {
        return ""
    }
    return "<gradient:$LOW_HEALTH_HEX:${healthHex(fraction)}>$text</gradient>"
}

fun healthHex(fraction: Double): String {
    val rgb = Color.HSBtoRGB((fraction.coerceIn(0.0, 1.0) * 120 / 360).toFloat(), 0.85f, 0.95f) and 0xFFFFFF
    return "#%06X".format(rgb)
}
