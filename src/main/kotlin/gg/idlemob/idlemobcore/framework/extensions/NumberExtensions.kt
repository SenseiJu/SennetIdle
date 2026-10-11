package gg.idlemob.idlemobcore.framework.extensions

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

private val suffixes = listOf("K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc")

private val formatter = ThreadLocal.withInitial {
    DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US)).apply {
        roundingMode = RoundingMode.DOWN
    }
}

fun Number.formatSuffixed(): String {
    val value = toDouble()
    if (value.isNaN() || value.isInfinite()) {
        return value.toString()
    }

    val abs = Math.abs(value)
    if (abs < 1000) {
        return formatter.get().format(value)
    }

    val tier = (Math.log10(abs) / 3).toInt()
    if (tier > suffixes.size) {
        return "%.2e".format(Locale.US, value)
    }

    return formatter.get().format(value / Math.pow(1000.0, tier.toDouble())) + suffixes[tier - 1]
}

fun String.parseSuffixed(): Double? {
    val text = trim().replace(",", "").replace("_", "")
    text.toDoubleOrNull()?.let { return it }

    val (index, suffix) = suffixes.withIndex()
        .filter { text.length > it.value.length && text.endsWith(it.value, ignoreCase = true) }
        .maxByOrNull { it.value.length }
        ?: return null

    val number = text.dropLast(suffix.length).trim().toDoubleOrNull() ?: return null
    return number * Math.pow(1000.0, index + 1.0)
}
