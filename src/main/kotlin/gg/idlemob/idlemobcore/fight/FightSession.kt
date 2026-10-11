package gg.idlemob.idlemobcore.fight

import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import gg.idlemob.idlemobcore.framework.text.healthBar as healthBarText
import gg.idlemob.idlemobcore.framework.text.healthGradient
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.plugin
import gg.idlemob.idlemobcore.stats.Stat
import gg.idlemob.idlemobcore.stats.StatContext
import gg.idlemob.idlemobcore.stats.Stats
import gg.idlemob.idlemobcore.user.User
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import kotlin.random.Random

private const val NAMETAG_BAR_LENGTH = 30
private const val TICKS_PER_SECOND = 20L

class FightSession(
    val player: Player,
    val user: User,
    private val manager: FightManager,
    private val location: Location,
) {
    lateinit var mob: LivingEntity
        private set
    lateinit var stats: Stats
        private set
    var boss = false
        private set
    var health = 0.0
        private set
    var maxHealth = 0.0
        private set
    var bossTicksLeft = 0L
        private set
    val bossSecondsLeft: Long
        get() = (bossTicksLeft + TICKS_PER_SECOND - 1) / TICKS_PER_SECOND

    private var removed = false
    private var ticks = 0L
    private var lastHitTick = -20
    private val clicks = ArrayDeque<Long>()
    private val healthBar = BossBar.bossBar(Component.empty(), 1F, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
    private val timerBar = BossBar.bossBar(Component.empty(), 1F, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS)

    val cps: Int
        get() {
            pruneClicks()
            return clicks.size
        }

    init {
        player.showBossBar(healthBar)
        spawnMob()
    }

    fun tick() {
        if (removed) {
            return
        }

        val newSecond = ticks++ % TICKS_PER_SECOND == 0L
        if (newSecond) {
            refreshStats()
            pruneClicks()
            updateActionBar()
        }

        val current = mob
        damage(stats[Stat.DPS] / TICKS_PER_SECOND)

        if (boss && mob === current) {
            bossTicksLeft--
            if (bossTicksLeft <= 0) {
                failBoss()
            } else {
                updateBars()
            }
        }
    }

    fun click() {
        pruneClicks()
        clicks.addLast(System.currentTimeMillis())

        val tick = player.server.currentTick
        val cooldownTicks = 20 / stats[Stat.CPS_LIMIT].coerceAtLeast(1.0)
        if (tick - lastHitTick < cooldownTicks) {
            return
        }

        lastHitTick = tick

        if (!boss && Random.nextDouble(100.0) < stats[Stat.INSTANT_KILL_CHANCE]) {
            damage(health)
            return
        }

        val crit = Random.nextDouble(100.0) < stats[Stat.CRIT_CHANCE]
        damage(stats[Stat.DPS] * stats[Stat.CLICK_DAMAGE_PERCENT] / 100 * (if (crit) 2 else 1))
    }

    fun restart() {
        if (removed) {
            return
        }

        manager.removeMob(mob)
        spawnMob()
    }

    fun remove() {
        removed = true
        manager.removeMob(mob)
        player.hideBossBar(healthBar)
        player.hideBossBar(timerBar)
    }

    private fun spawnMob() {
        val stages = plugin.cfg.stages
        user.wave = user.wave.coerceIn(1, stages.wavesPerStage)
        boss = user.wave == stages.wavesPerStage

        refreshStats()
        maxHealth = stages.mobHealth(user.stage, boss)
        health = maxHealth

        val type = (if (boss) stages.bossTypes else stages.mobTypes).random()
        mob = manager.spawnMob(this, location, type)

        if (boss) {
            bossTicksLeft = (stats[Stat.BOSS_TIME_LIMIT] * TICKS_PER_SECOND).toLong().coerceAtLeast(TICKS_PER_SECOND)
            player.showBossBar(timerBar)
            player.showTitle(Title.title(plugin.messages.fight.bossSpawnedTitle(), plugin.messages.fight.bossSpawnedSubtitle()))
        } else {
            player.hideBossBar(timerBar)
        }

        updateBars()
        updateActionBar()
    }

    private fun damage(amount: Double) {
        if (removed || amount <= 0) {
            return
        }

        health -= amount
        if (health <= 0) {
            kill()
        } else {
            updateBars()
        }
    }

    private fun kill() {
        val stage = user.stage
        val wave = user.wave
        val wasBoss = boss

        val drops = manager.drops.roll(user, stats, wasBoss)
        drops.forEach { (resource, amount) -> user.addResource(resource, amount) }

        manager.removeMob(mob)

        if (wasBoss) {
            user.stage++
            user.wave = 1
            player.sendMessage(plugin.messages.fight.stageCompleted(stage))
            plugin.prestige.notifyIfReady(player, user)
            manager.callEvent(StageCompletedEvent(player, user, stage))
            EventLogger.info("stage_completed", player) { "stage" to stage }
        } else {
            user.wave++
        }

        manager.callEvent(MobKilledEvent(player, user, stage, wave, wasBoss, drops))
        EventLogger.debug("mob_killed", player) {
            "stage" to stage
            "wave" to wave
            "boss" to wasBoss
            "drops" to drops.keys.toList()
        }

        spawnMob()
    }

    private fun failBoss() {
        val stage = user.stage

        manager.removeMob(mob)
        user.wave = 1
        player.sendMessage(plugin.messages.fight.bossFailed(stage))
        manager.callEvent(BossFailedEvent(player, user, stage))
        EventLogger.debug("boss_failed", player) { "stage" to stage }

        spawnMob()
    }

    private fun refreshStats() {
        stats = manager.stats.calculate(StatContext(player, user, boss), manager.baseStats())
    }

    private fun pruneClicks() {
        val cutoff = System.currentTimeMillis() - 1000
        while (clicks.isNotEmpty() && clicks.first() < cutoff) {
            clicks.removeFirst()
        }
    }

    private fun updateBars() {
        val fraction = (health / maxHealth).coerceIn(0.0, 1.0)

        healthBar.name(
            mini(
                if (boss) "<dark_red><b>BOSS</b> <dark_gray>| <health>" else "<health>",
                Placeholder.parsed("health", healthGradient(fraction, "❤ ${health.coerceAtLeast(0.0).formatSuffixed()}")),
            ),
        )
        healthBar.progress(fraction.toFloat())
        healthBar.color(
            when {
                fraction > 0.5 -> BossBar.Color.GREEN
                fraction > 0.2 -> BossBar.Color.YELLOW
                else -> BossBar.Color.RED
            },
        )

        mob.customName(mini(healthBarText(fraction, NAMETAG_BAR_LENGTH)))
        mob.isCustomNameVisible = true

        if (boss) {
            val limitTicks = (stats[Stat.BOSS_TIME_LIMIT] * TICKS_PER_SECOND).coerceAtLeast(1.0)
            timerBar.name(mini("<light_purple>Time left: <white><seconds>s", Placeholder.unparsed("seconds", bossSecondsLeft.toString())))
            timerBar.progress((bossTicksLeft / limitTicks).toFloat().coerceIn(0F, 1F))
        }
    }

    private fun updateActionBar() {
        player.sendActionBar(mini(
            if (boss) "<gray>Stage <white><stage></white> <dark_gray>• <dark_red><b>BOSS WAVE" else "<gray>Stage <white><stage></white> <dark_gray>• <gray>Wave <white><wave></white>/<white><waves>",
            Placeholder.unparsed("stage", user.stage.toString()),
            Placeholder.unparsed("wave", user.wave.toString()),
            Placeholder.unparsed("waves", plugin.cfg.stages.wavesPerStage.toString()),
        ))
    }
}
