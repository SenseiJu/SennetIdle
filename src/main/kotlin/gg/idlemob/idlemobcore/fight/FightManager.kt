package gg.idlemob.idlemobcore.fight

import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.stats.Stat
import gg.idlemob.idlemobcore.stats.StatService
import gg.idlemob.idlemobcore.user.UserManager
import io.papermc.paper.event.entity.EntityKnockbackEvent
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTransformEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class FightManager(
    private val plugin: IdleMobCore,
    private val users: UserManager,
    val stats: StatService,
    val drops: DropRoller,
) : Listener {
    private val sessions = HashMap<UUID, FightSession>()
    private val mobs = HashMap<UUID, FightSession>()

    private val ticker = plugin.server.scheduler.runTaskTimer(plugin, Runnable { sessions.values.toList().forEach(FightSession::tick) }, 1, 1)

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    operator fun get(player: Player): FightSession = sessions[player.uniqueId] ?: throw IllegalStateException("${player.name} has no fight session")

    fun find(player: Player): FightSession? = sessions[player.uniqueId]

    fun restart(player: Player) {
        sessions[player.uniqueId]?.restart()
    }

    fun shutdown() {
        ticker.cancel()
        sessions.values.forEach(FightSession::remove)
        sessions.clear()
        mobs.clear()
    }

    fun baseStats(): Map<Stat, Double> = mapOf(
        Stat.DPS to plugin.cfg.playerAttack.baseDps,
        Stat.CLICK_DAMAGE_PERCENT to plugin.cfg.playerAttack.clickDamagePercent,
        Stat.CPS_LIMIT to plugin.cfg.playerAttack.baseCpsLimit.toDouble(),
        Stat.DROP_CHANCE to 1.0,
        Stat.DROP_AMOUNT to 1.0,
        Stat.BOSS_TIME_LIMIT to plugin.cfg.stages.baseBossTimeLimit.seconds.toDouble(),
        Stat.CRIT_CHANCE to 0.0,
        Stat.INSTANT_KILL_CHANCE to 0.0,
        Stat.BOX_DROP_CHANCE to 1.0,
    )

    fun callEvent(event: Event) {
        plugin.server.pluginManager.callEvent(event)
    }

    internal fun spawnMob(session: FightSession, location: Location, type: EntityType): LivingEntity {
        @Suppress("UNCHECKED_CAST")
        val mob = location.world.spawn(location, type.entityClass as Class<LivingEntity>) {
            it.isVisibleByDefault = false
            it.isPersistent = false
            it.isSilent = true
            it.maximumNoDamageTicks = 0
            it.setAI(false)
        }

        session.player.showEntity(plugin, mob)
        mobs[mob.uniqueId] = session
        return mob
    }

    internal fun removeMob(mob: LivingEntity) {
        mobs.remove(mob.uniqueId)
        mob.remove()
    }

    private fun startSession(player: Player) {
        val user = users[player.uniqueId] ?: return

        sessions[player.uniqueId] = FightSession(player, user, this, plugin.cfg.fightZone.mobLocation.toLocation())
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        startSession(event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        sessions.remove(event.player.uniqueId)?.remove()
    }

    @EventHandler(priority = EventPriority.HIGH)
    private fun onEntityDamage(event: EntityDamageEvent) {
        val session = mobs[event.entity.uniqueId] ?: return

        if ((event as? EntityDamageByEntityEvent)?.damager != session.player) {
            event.isCancelled = true
            return
        }

        event.damage = 0.0
        session.click()
    }

    @EventHandler
    private fun onEntityKnockback(event: EntityKnockbackEvent) {
        if (event.entity.uniqueId in mobs) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onEntityCombust(event: EntityCombustEvent) {
        if (event.entity.uniqueId in mobs) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onEntityTransform(event: EntityTransformEvent) {
        if (event.entity.uniqueId in mobs) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onEntityDeath(event: EntityDeathEvent) {
        if (mobs.remove(event.entity.uniqueId) != null) {
            event.drops.clear()
            event.droppedExp = 0
        }
    }
}
