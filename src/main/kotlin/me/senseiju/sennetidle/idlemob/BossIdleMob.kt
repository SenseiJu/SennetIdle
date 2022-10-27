package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.extensions.component
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.EntityType

private val BOSS_SPAWN_TITLE = Title.title(
    "<red><b><obf>||</obf> BOSS WAVE <obf>||</obf>".component(),
    "<grey>Kill the boss before the time runs out!".component(),
    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(20))
)

private val BOSS_KILLED_SUCCESS_SOUND = Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1F, 1F)

class BossIdleMob(
    user: User,
    private val location: Location,
    entityType: EntityType,
    maxHealth: Long
) : AbstractIdleMob(user, location, entityType, maxHealth) {

    private val timerBarKey = NamespacedKey(plugin, "idle-mob-timer-${getPlayerUUID().toString().lowercase()}")
    private val timerBar = plugin.server.createBossBar(timerBarKey, "Time Remaining", BarColor.PINK, BarStyle.SOLID)

    private val maxTime = 10L
    private var timeRemaining = maxTime

    init {
        user.idleMob = this

        timerBar.removeAll()
        timerBar.progress = timeRemaining / maxTime.toDouble()

        runnables.addRepeatingRunnable(20) {
            timerBar.progress = --timeRemaining / maxTime.toDouble()
            currentHealth -= (user.dps + user.bossDps)

            if (currentHealth <= 0 || timeRemaining <= 0) {
                entity.health = 0.0
            }

            updateEntityHealthVisuals()
        }

        user.withPlayer {
            it.setPlayerTime(18000, false)
            it.showTitle(BOSS_SPAWN_TITLE)

            timerBar.addPlayer(it)
        }
    }

    override fun onHit() {

    }

    override fun onKill(success: Boolean) {
        if (success) {
            user.withPlayer {
                it.playSound(BOSS_KILLED_SUCCESS_SOUND)
            }
        } else {
            user.currentWave = (user.currentWave - (BOSS_WAVE_INTERVAL - 1)).coerceAtLeast(1)
        }
    }

    override fun dispose() {
        timerBar.removeAll()
        plugin.server.removeBossBar(timerBarKey)

        super.dispose()
    }
}