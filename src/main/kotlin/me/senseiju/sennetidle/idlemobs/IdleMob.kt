package me.senseiju.sennetidle.idlemobs

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.users.User
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.runnables.CountdownRunnable
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.KeyedBossBar
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.roundToLong

private val plugin = JavaPlugin.getPlugin(SennetIdle::class.java)
private val protocolManager = ProtocolLibrary.getProtocolManager()

class IdleMob(
    type: EntityType,
    spawnLocation: Location,
    private val maxHealth: Double,
    private val user: User,
    private var timeLimit: Int = -1
) : CountdownRunnable(), Listener {
    override var timeToComplete: Long = timeLimit.toLong()

    private var currentHealth = maxHealth
    private val entity = spawnLocation.world.spawnEntity(spawnLocation, type) as LivingEntity
    private lateinit var timeLimitKey: NamespacedKey
    private lateinit var timeLimitBossBar: KeyedBossBar

    var progressPlayer = false

    init {
        entity.setAI(false)
        entity.isSilent = true
        entity.isCustomNameVisible = true

        hideMobFromAllPlayers(user.getBukkitPlayer())

        plugin.registerEvents(this)

        if (timeLimit > 0) {
            timeLimitKey = NamespacedKey(plugin, "idle-mob-time-limit-${user.uuid.toString().lowercase()}")
            timeLimitBossBar = plugin.server.createBossBar(timeLimitKey, "Time remaining", BarColor.WHITE, BarStyle.SOLID)
            user.getBukkitPlayer()?.let { timeLimitBossBar.addPlayer(it) }
            start(plugin)
        }
    }

    /**
     * This is where logic for when the time has run out to kill a mob. Used for boss's
     */
    override fun onComplete() {
        dispose()
    }

    override fun executeEverySecond() {
        timeLimitBossBar.progress = timeToComplete.toDouble() / timeLimit
    }

    fun applyPassiveDPS() {
        currentHealth -= user.passiveDPS

        if (currentHealth <= 0) {
            entity.health = 0.0
        }

        updateEntityCustomName()
    }

    fun healthDecimal() = currentHealth / maxHealth

    fun isEntityDead() = entity.isDead

    fun dispose() {
        HandlerList.unregisterAll(this)
        if (!entity.isDead) entity.remove()
        if (timeToComplete != -1L) cancel()
        if (this::timeLimitBossBar.isInitialized && this::timeLimitKey.isInitialized) {
            timeLimitBossBar.removeAll()
            plugin.server.removeBossBar(timeLimitKey)
        }
    }

    private fun updateEntityCustomName() {
        entity.customName(
            text(
                "${currentHealth.roundToLong().coerceAtLeast(0)}/${maxHealth.roundToLong()}",
                NamedTextColor.DARK_GREEN,
                TextDecoration.BOLD
            )
        )
    }

    private fun hideMobFromAllPlayers(excludedPlayer: Player?) {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
        packet.intLists.writeSafely(0, mutableListOf(entity.entityId))

        for (player in Bukkit.getOnlinePlayers().filter { player -> player.uniqueId != excludedPlayer?.uniqueId }) {
            protocolManager.sendServerPacket(player, packet)
        }
    }

    @EventHandler
    private fun onEntityDamageByEntityEvent(e: EntityDamageByEntityEvent) {
        if (e.entity.uniqueId != entity.uniqueId || e.damager !is Player || e.damager.uniqueId != user.uuid) return

        e.damage = Double.MIN_VALUE

        applyPassiveDPS()
    }

    @EventHandler
    private fun onEntityDeathEvent(e: EntityDeathEvent) {
        if (e.entity.uniqueId != entity.uniqueId) return

        dispose()

        progressPlayer = true
    }
}