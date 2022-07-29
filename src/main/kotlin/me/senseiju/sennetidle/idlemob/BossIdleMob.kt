package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.user.User
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDeathEvent

private val BOSS_SPAWN_TITLE = Title.title(
    Component.text("|| BOSS WAVE ||", NamedTextColor.RED, TextDecoration.BOLD),
    Component.text("Kill the boss before the time runs out!", NamedTextColor.GRAY),
    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(20))
)

private val BOSS_KILLED_SUCCESS_SOUND = Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1F, 1F)

class BossIdleMob(
    user: User,
    location: Location,
    entityType: EntityType,
    maxHealth: Long
) : IdleMob(user, location, entityType, maxHealth) {
    private var wasKilledByPlayer = false

    private val timerBarKey = NamespacedKey(plugin, "idle-mob-timer-${playerId.toString().lowercase()}")
    private val timerBar = plugin.server.createBossBar(timerBarKey, "Time Remaining", BarColor.PINK, BarStyle.SOLID)

    private val maxTime = 10L
    private var timeRemaining = maxTime

    init {
        timerBar.removeAll()
        timerBar.progress = timeRemaining / maxTime.toDouble()

        runnables.addRepeatingRunnable(20) {
            timerBar.progress = --timeRemaining / maxTime.toDouble()

            if (timeRemaining <= 0) {
                entity.health = 0.0
            }
        }

        user.withPlayer {
            it.setPlayerTime(18000, false)
            it.showTitle(BOSS_SPAWN_TITLE)

            timerBar.addPlayer(it)
        }
    }

    override fun dispose() {
        super.dispose()

        user.withPlayer {

        }

        timerBar.removeAll()
        plugin.server.removeBossBar(timerBarKey)
    }

    override fun onEntityDeathEvent(e: EntityDeathEvent) {
        if (currentHealth < 0) {
            wasKilledByPlayer = true
            user.withPlayer {
                it.playSound(BOSS_KILLED_SUCCESS_SOUND)
            }

            super.onEntityDeathEvent(e)

            return
        }
        user.currentWave = (user.currentWave - (BOSS_WAVE_INTERVAL - 1)).coerceAtLeast(1)

        dispose()
    }
}