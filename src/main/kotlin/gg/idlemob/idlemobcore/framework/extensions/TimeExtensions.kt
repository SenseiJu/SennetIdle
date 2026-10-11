package gg.idlemob.idlemobcore.framework.extensions

fun Long.formatDuration(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60

    return buildList {
        if (hours > 0) add("${hours}h")
        if (minutes > 0) add("${minutes}m")
        if (seconds > 0 || isEmpty()) add("${seconds}s")
    }.joinToString(" ")
}
