package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sentils.registerEvents
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToLong

const val BOSS_WAVE_INTERVAL = 10
private const val BOSS_IDLE_MOB_BASE_HEALTH = 250.0
val BOSS_ENTITY_TYPES =
    setOf(
        EntityType.IRON_GOLEM,
        EntityType.WARDEN
    )

private const val IDLE_MOB_BASE_HEALTH = 100.0
val REGULAR_ENTITY_TYPES =
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

private val userService = serviceProvider.get<UserService>()

class IdleMobSpawnHandler : Listener {
    val idleMobs = hashMapOf<UUID, AbstractIdleMob>()

    private val spawnLocation = plugin.config.getLocation("idle-mob.spawn-location") ?: throw Exception("'idle-mob.spawn-location' config option not set!")

    init {
        plugin.registerEvents(this)
    }

    fun disposeAllMobs() {
        idleMobs.values.forEach {
            it.dispose()
        }
    }

    fun spawnNewIdleMob(user: User) {
        spawnNewIdleMob(user, user.currentWave % BOSS_WAVE_INTERVAL == 0)
    }

    private fun spawnNewIdleMob(user: User, isBoss: Boolean) {
        user.idleMob?.dispose()

        val idleMob = if (isBoss) {
            BossIdleMob(
                user,
                spawnLocation,
                BOSS_ENTITY_TYPES.random(),
                calculateMobHealth(BOSS_IDLE_MOB_BASE_HEALTH, user.currentWave)
            )
        } else {
            IdleMob(
                user,
                spawnLocation,
                REGULAR_ENTITY_TYPES.random(),
                calculateMobHealth(IDLE_MOB_BASE_HEALTH, user.currentWave)
            )
        }

        idleMobs[idleMob.getEntityUUID()] = idleMob
    }

    @EventHandler
    private fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        if (!e.player.isOnline) {
            return
        }

        idleMobs.values.filter {
            it.getPlayerUUID() == e.player.uniqueId
        }.let {
            if (it.isEmpty()) {
                spawnNewIdleMob(userService.getUser(e.player.uniqueId))
            }
        }

        idleMobs.values.forEach {
            if (it.getPlayerUUID() != e.player.uniqueId) {
                @Suppress("UnstableApiUsage")
                e.player.hideEntity(plugin, it.entity)
            }
        }
    }

    @EventHandler
    private fun onEntityDamageByEntityEvent(e: EntityDamageByEntityEvent) {
        idleMobs[e.entity.uniqueId]?.let {
            if (e.damager !is Player || e.damager.uniqueId != it.getPlayerUUID()) {
                e.isCancelled = true
                return
            }

            e.damage = Double.MIN_VALUE

            it.onDamageByPlayer()
        }
    }

    @EventHandler
    private fun onEntityCombustEvent(e: EntityCombustEvent) {
        idleMobs[e.entity.uniqueId]?.let {
            e.isCancelled = true
        }
    }

    @EventHandler
    private fun onEntityDeathEvent(e: EntityDeathEvent) {
        val idleMob = idleMobs.remove(e.entity.uniqueId) ?: return
        idleMob.onDeath()

        val player = Bukkit.getPlayer(idleMob.getPlayerUUID()) ?: return
        if (player.isOnline) {
            spawnNewIdleMob(userService.getUser(player.uniqueId))
        }
    }

    private fun calculateMobHealth(baseHealth: Double, currentWave: Int) = (baseHealth * (1.15.pow(currentWave))).roundToLong()
}