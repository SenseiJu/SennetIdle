package me.senseiju.sennetidle.utils.extensions

import me.senseiju.sennetidle.plugin
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitTask

fun Listener.register() {
    plugin.server.pluginManager.registerEvents(this, plugin)
}

fun Player.playSound(sound: Sound, volume: Float = 1F, pitch: Float = 1F) {
    playSound(location, sound, volume, pitch)
}

fun runTask(block: () -> Unit): BukkitTask {
    return plugin.server.scheduler.runTask(plugin, Runnable(block))
}

fun runTaskTimer(delay: Long, period: Long, block: () -> Unit): BukkitTask {
    return plugin.server.scheduler.runTaskTimer(plugin, Runnable(block), delay, period)
}

fun runTaskTimerAsync(delay: Long, period: Long, block: () -> Unit): BukkitTask {
    return plugin.server.scheduler.runTaskTimerAsynchronously(plugin, Runnable(block), delay, period)
}
