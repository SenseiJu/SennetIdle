package me.senseiju.sennetidle.idlemob

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.reagents.reagentData.DroppableReagent
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.MultiRunnable
import me.senseiju.sentils.extensions.primitives.asCurrencyFormat
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import java.util.*
import kotlin.math.roundToLong

private val protocolManager = ProtocolLibrary.getProtocolManager()

open class IdleMob(
    protected val user: User,
    location: Location,
    entityType: EntityType,
    private val maxHealth: Long
) {
    val playerId = user.playerId

    protected var currentHealth = maxHealth
    private val healthBarKey = NamespacedKey(plugin, "idle-mob-health-${playerId.toString().lowercase()}")
    private val healthBar = plugin.server.createBossBar(healthBarKey, "Health", BarColor.RED, BarStyle.SOLID)

    protected val entity = location.world.spawnEntity(location, entityType) as LivingEntity

    private val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

    protected val runnables = MultiRunnable()

    var cps = 0
        private set

    init {
        user.idleMobId = getUniqueId()

        healthBar.removeAll()

        entity.setAI(false)
        entity.isSilent = true
        entity.maximumNoDamageTicks = 5
        entity.removeWhenFarAway = false

        packet.intLists.writeSafely(0, mutableListOf(entity.entityId))

        runnables.addRepeatingRunnable(20) {
            currentHealth -= user.dps

            if (currentHealth <= 0) {
                entity.health = 0.0
            }

            updateEntityHealthVisuals()
            sendRemoveEntityFromPlayerViewPacket()
        }
        runnables.start(plugin, false)

        user.withPlayer {
            it.setPlayerTime(6000, false)

            healthBar.addPlayer(it)
        }

        sendRemoveEntityFromPlayerViewPacket()
        updateEntityHealthVisuals()
    }

    open fun dispose() {
        entity.remove()

        healthBar.removeAll()
        plugin.server.removeBossBar(healthBarKey)

        runnables.cancel()
    }

    fun getUniqueId(): UUID {
        return entity.uniqueId
    }

    fun mobHealthString(): String {
        return "${currentHealth.coerceAtLeast(0).asCurrencyFormat()}/${maxHealth.asCurrencyFormat()}"
    }

    private fun updateEntityHealthVisuals() {
        healthBar.progress = currentHealth.coerceAtLeast(0).toDouble() / maxHealth
    }

    private fun sendRemoveEntityFromPlayerViewPacket() {
        for (player in Bukkit.getOnlinePlayers().filter { player -> player.uniqueId != playerId }) {
            protocolManager.sendServerPacket(player, packet)
        }
    }

    fun onEntityDamageByEntityEvent(e: EntityDamageByEntityEvent) {
        if (e.damager !is Player || e.damager.uniqueId != playerId) {
            e.isCancelled = true
            return
        }

        e.damage = Double.MIN_VALUE

        currentHealth -= (user.dps * 0.2).roundToLong()

        if (currentHealth <= 0) {
            entity.health = 0.0
        }

        cps++
        runnables.addRepeatingRunnable(20, 1) { cps-- }

        updateEntityHealthVisuals()
    }

    fun onEntityCombustEvent(e: EntityCombustEvent) {
        e.isCancelled = true
    }

    open fun onEntityDeathEvent(e: EntityDeathEvent) {
        Reagent.droppable.forEach {
            val data = it.dataAs<DroppableReagent>()

            if (user.currentWave < data.waveUnlock) {
                return@forEach
            }

            if (data.shouldDrop()) {
                user.addReagent(it, data.randomAmount())
            }
        }

        user.currentWave++

        dispose()
    }
}