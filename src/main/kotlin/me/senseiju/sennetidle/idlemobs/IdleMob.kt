package me.senseiju.sennetidle.idlemobs

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.users.User
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.runnables.newRunnable
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.pow

private const val ONE_SECOND_IN_TICKS = 20L
private const val TEMP_BASE_HEALTH = 100.0

private val ENTITY_POOL = setOf(EntityType.ZOMBIE, EntityType.BLAZE, EntityType.SKELETON)

private val protocolManager = ProtocolLibrary.getProtocolManager()

class IdleMob(
    private val plugin: SennetIdle,
    private val user: User,
    private val spawnLocation: Location
) : BukkitRunnable(), Listener {
    private lateinit var activeEntity: LivingEntity
    private val healthBar = plugin.server.createBossBar("Mob Health", BarColor.RED, BarStyle.SOLID)

    init {
        plugin.registerEvents(this)

        respawnNextMob()

        runTaskTimer(plugin, ONE_SECOND_IN_TICKS, ONE_SECOND_IN_TICKS)
    }

    fun dispose() {
        activeEntity.remove()

        HandlerList.unregisterAll(this)
    }

    private fun applyDamage() {
        if (activeEntity.health - user.passiveDPS <= 0) {
            activeEntity.health = 0.0
        } else {
            activeEntity.health = activeEntity.health - user.passiveDPS
        }

        updateBossBar()
    }

    private fun respawnNextMob() {
        if (this::activeEntity.isInitialized && !activeEntity.isDead) {
            activeEntity.remove()
        }

        val health = TEMP_BASE_HEALTH * (1.15.pow(user.currentWave))
        val entity = spawnLocation.world.spawnEntity(spawnLocation, ENTITY_POOL.random()) as LivingEntity
        entity.setAI(false)
        entity.registerAttribute(Attribute.GENERIC_MAX_HEALTH)
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = health
        entity.health = health

        activeEntity = entity

        hideMobFromAllPlayers(plugin.server.getPlayer(user.uuid))
    }

    private fun hideMobFromAllPlayers(excludedPlayer: Player?) {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
        packet.intLists.writeSafely(0, mutableListOf(activeEntity.entityId))

        for (player in plugin.server.onlinePlayers.filter { player -> player.uniqueId != excludedPlayer?.uniqueId }) {
            protocolManager.sendServerPacket(player, packet)
        }
    }

    private fun updateBossBar() {
        healthBar.progress = activeEntity.health / activeEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
    }

    //region Event listeners

    @EventHandler
    private fun onPlayerJoin(e: PlayerJoinEvent) {
        if (e.player.uniqueId == user.uuid ) {
            healthBar.addPlayer(e.player)
        }
    }

    @EventHandler
    private fun onEntityByPlayerDamage(e: EntityDamageByEntityEvent) {
        if (e.entity.uniqueId != activeEntity.uniqueId) return

        if (e.damager.uniqueId == user.uuid) {
            e.damage = Double.MIN_VALUE

            applyDamage()
        } else if (e.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            e.isCancelled = true
        }
    }

    @EventHandler
    private fun onEntityDeath(e: EntityDeathEvent) {
        if (e.entity.uniqueId == activeEntity.uniqueId) {
            //user.currentWave += 1

            newRunnable { respawnNextMob() }.runTaskLater(plugin, 20)
        }
    }

    //endregion

    //region Bukkit runnable

    override fun run() {
        applyDamage()
    }

    //endregion
}

private val particleIds = setOf(0, 5)

class IdleMobParticleListener(
    plugin: SennetIdle
) : PacketAdapter(plugin, PacketType.Play.Server.WORLD_PARTICLES) {

    init {
        protocolManager.addPacketListener(this)
    }

    override fun onPacketSending(event: PacketEvent) {
        if (event.packet.integers.values.any { id ->  particleIds.contains(id)}) {
            event.isCancelled = true
        }
    }
}