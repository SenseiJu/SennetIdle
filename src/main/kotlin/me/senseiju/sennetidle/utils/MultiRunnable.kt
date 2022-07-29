package me.senseiju.sennetidle.utils

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

open class MultiRunnable : BukkitRunnable() {
    private val runnables = arrayListOf<RepeatingRunnable>()

    private var started = false

    fun start(plugin: JavaPlugin, async: Boolean = false) {
        if (started) {
            return
        }

        if (async) {
            runTaskTimerAsynchronously(plugin, 0L, 1L)
        } else {
            runTaskTimer(plugin, 0L, 1L)
        }

        started = true
    }

    fun addRepeatingRunnable(tickCycle: Long, toRun: () -> Unit) {
        runnables.add(RepeatingRunnable(tickCycle, -1, toRun))
    }

    fun addRepeatingRunnable(tickCycle: Long, maxExecutes: Long, toRun: () -> Unit) {
        runnables.add(RepeatingRunnable(tickCycle, maxExecutes, toRun))
    }

    override fun run() {
        runnables.forEach {
            it.tryExecute()
        }

        runnables.removeIf {
            !it.isExecutable()
        }
    }
}

private class RepeatingRunnable(
    private val tickCycle: Long,
    private var maxExecutes: Long,
    private val toRun: () -> Unit
) {
    private var tickCounter = 0

    fun isExecutable(): Boolean {
        return (maxExecutes == -1L) || (maxExecutes > 0)
    }

    fun tryExecute() {
        if (++tickCounter >= tickCycle) {
            tickCounter = 0

            if (maxExecutes > 0) {
                maxExecutes--
            }


            toRun()
        }
    }
}