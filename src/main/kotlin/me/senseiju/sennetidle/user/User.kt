package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.idlemob.AbstractIdleMob
import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.powers.Power
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.utils.message
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
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
    var powerCooldowns: EnumMap<Power, Long> = EnumMap(Power::class.java)

    init {
        Reagent.damaging.forEach {
            onAddReagent(it, getReagentAmount(it))
        }
    }

    companion object {
        /**
         * Generates a blank user
         *
         * @param playerId the players UUID
         *
         * @return a blank user
         */
        fun new(playerId: UUID): User {
            return User(playerId, 1, 0, 0, Reagent.emptyUserMap(), Upgrade.emptyUserMap())
        }
    }

    /**
     * Adds money to the user with [Economy]
     *
     * If a negative amount is sent, it will automatically be replaced with 0
     *
     * @param amount the amount to add (NO NEGATIVES PLEASE)
     */
    fun addMoney(amount: Double) {
        econ.depositPlayer(Bukkit.getOfflinePlayer(playerId), amount.coerceAtLeast(0.0))
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

    /**
     * Checks if the user has enough promotions to access the specified [Reagent]
     *
     * @param reagent the reagent to check access for
     *
     * @return true if user has enough promotions for the [Reagent]
     */
    fun hasEnoughPromotions(reagent: Reagent): Boolean {
        return promotions >= reagent.data.promotionUnlock
    }

    /**
     * Checks if the user has met the requirements to promote
     *
     * @return true if requirements met
     */
    fun canPromote(): Boolean {
        return currentWave > 50 + (promotions * 30)
    }

    /**
     * Executes the block if the [Player] is available otherwise nothing will happen
     */
    fun withPlayer(block: (Player) -> Unit) {
        Bukkit.getPlayer(playerId)?.let { block(it) }
    }

    /**
     * Sends a message to the user if the player is online, uses [withPlayer] to get the player
     *
     * @param msg the message
     * @param tagResolvers any tag resolvers to apply
     */
    fun message(msg: Message, vararg tagResolvers: TagResolver.Single = emptyArray()) {
        withPlayer { it.message(msg, *tagResolvers) }
    }

    override fun addUpgradeLevel(upgrade: Upgrade, level: Int) {
        unspentUpgradePoints = (unspentUpgradePoints - level).coerceAtLeast(0)
        super.addUpgradeLevel(upgrade, level)
    }

    override fun canLevelUpgrade(upgrade: Upgrade): Boolean {
        return unspentUpgradePoints > 0 && super.canLevelUpgrade(upgrade)
    }
}