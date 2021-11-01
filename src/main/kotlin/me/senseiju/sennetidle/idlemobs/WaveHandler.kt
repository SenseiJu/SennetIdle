package me.senseiju.sennetidle.idlemobs

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.users.User
import me.senseiju.sentils.extensions.primitives.color
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.pow

private const val IDLE_MOB_BASE_HEALTH = 100.0
private const val BOSS_IDLE_MOB_BASE_HEALTH = 250.0

private val BOSS_ENTITY_TYPES =
    setOf(
        EntityType.IRON_GOLEM,
        EntityType.GIANT,
        EntityType.LLAMA
    )
private val REGULAR_ENTITY_TYPES =
    setOf(
        EntityType.SKELETON,
        EntityType.BLAZE,
        EntityType.ZOMBIE,
        EntityType.ZOMBIFIED_PIGLIN,
        EntityType.SPIDER,
        EntityType.CREEPER,
        EntityType.ENDERMAN,
        EntityType.EVOKER,
        EntityType.VINDICATOR,
        EntityType.PILLAGER
    )

private val plugin = JavaPlugin.getPlugin(SennetIdle::class.java)

class WaveHandler(
    player: Player,
    private val user: User,
    private val spawnLocation: Location,
    private val bossWaveInterval: Int,
    private val bossWaveTimeLimit: Int,
) : BukkitRunnable() {
    private lateinit var idleMob: IdleMob
    private val bossBarKey = NamespacedKey(plugin, "idle-mob-${user.uuid.toString().lowercase()}")
    private val bossBar = plugin.server.createBossBar(bossBarKey, "&c&lMob Health".color(), BarColor.RED, BarStyle.SOLID)

    init {
        bossBar.addPlayer(player)

        spawnNewMob()

        runTaskTimer(plugin, 0L, 20L)
    }

    override fun run() {
        if (idleMob.isEntityDead()) {
            updateUserProgress()
            spawnNewMob()
        }

        idleMob.applyPassiveDPS()
        bossBar.progress = idleMob.healthDecimal().coerceAtLeast(0.0)
    }

    fun dispose() {
        cancel()
        idleMob.dispose()
        plugin.server.removeBossBar(bossBarKey)
    }

    private fun updateUserProgress() {
        if (idleMob.progressPlayer) {
            user.currentWave++
            user.rewardWaveReagent()
        } else {
            user.currentWave -= bossWaveInterval - 1
        }
    }

    private fun spawnNewMob() {
        idleMob = if (user.currentWave % bossWaveInterval == 0) {
            user.getBukkitPlayer()?.setPlayerTime(18000, false)

            IdleMob(BOSS_ENTITY_TYPES.random(), spawnLocation, calculateMobHealth(BOSS_IDLE_MOB_BASE_HEALTH), user, bossWaveTimeLimit)
        } else {
            user.getBukkitPlayer()?.setPlayerTime(6000, false)

            IdleMob(REGULAR_ENTITY_TYPES.random(), spawnLocation, calculateMobHealth(IDLE_MOB_BASE_HEALTH), user)
        }
    }

    private fun calculateMobHealth(baseHealth: Double) = baseHealth * (1.15.pow(user.currentWave))
}