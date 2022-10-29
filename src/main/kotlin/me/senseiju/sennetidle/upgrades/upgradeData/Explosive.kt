package me.senseiju.sennetidle.upgrades.upgradeData

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.message
import me.senseiju.sennetidle.utils.randomChance
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle

object Explosive : IdleMobUpgrade {
    private const val chancePerLevel = 1.0F

    override val name  = "Explosive"
    override val material = Material.TNT
    override val modelData = 0
    override val description = "Instantly kill the next idle mob [Does not work on Bosses]"
    override val maxLevel = 25

    fun shouldProc(user: User): Boolean {
        return randomChance(user.getUpgradeLevel(Upgrade.EXPLOSIVE) * chancePerLevel)
    }

    fun handleProc(user: User, location: Location) {
        user.withPlayer {
            it.spawnParticle(Particle.EXPLOSION_HUGE, location, 1)
            it.message(Message.UPGRADE_EXPLOSIVE_PROCED)
        }
    }
}