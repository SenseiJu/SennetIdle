package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.idlemob.AbstractIdleMob
import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.upgrades.Upgrade
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

private val econ = plugin.server.servicesManager.getRegistration(Economy::class.java)?.provider ?: throw Exception("Vault not found")

class User(
    val playerId: UUID,
    var currentWave: Int,
    var promotions: Int,
    var unspentUpgradePoints: Int,
    override val reagents: EnumMap<Reagent, Int>,
    override val upgrades: EnumMap<Upgrade, Int>
) : ReagentHolder, UpgradeHolder {
    var idleMob: AbstractIdleMob? = null
    var dps: Long = 35
        private set
    var bossDps: Long = 0
        private set

    init {
        Reagent.damaging.forEach {
            onAddReagent(it, getReagentAmount(it))
        }
    }

    companion object {
        fun new(playerId: UUID): User {
            return User(playerId, 1, 0, 0, Reagent.emptyUserMap(), Upgrade.emptyUserMap())
        }
    }

    fun addMoney(amount: Double) {
        econ.depositPlayer(Bukkit.getOfflinePlayer(playerId), amount)
    }

    override fun onAddReagent(reagent: Reagent, amount: Int) {
        if (reagent.isDamaging()) {
            val damagingReagent = reagent.asDamageable()

            if (damagingReagent.bossOnly) {
                bossDps += damagingReagent.damagePerSecond * amount
            } else {
                dps += damagingReagent.damagePerSecond * amount
            }
        }
    }

    fun hasEnoughPromotions(reagent: Reagent): Boolean {
        return promotions >= reagent.data.promotionUnlock
    }

    fun canPromote(): Boolean {
        return currentWave > 50 + (promotions * 30)
    }

    /**
     * Executes the block if the [Player] is available otherwise nothing will happen
     */
    fun withPlayer(block: (Player) -> Unit) {
        Bukkit.getPlayer(playerId)?.let { block(it) }
    }

    override fun addUpgradeLevel(upgrade: Upgrade, level: Int) {
        unspentUpgradePoints = (unspentUpgradePoints - level).coerceAtLeast(0)
        super.addUpgradeLevel(upgrade, level)
    }

    override fun canLevelUpgrade(upgrade: Upgrade): Boolean {
        return unspentUpgradePoints > 0 && super.canLevelUpgrade(upgrade)
    }
}