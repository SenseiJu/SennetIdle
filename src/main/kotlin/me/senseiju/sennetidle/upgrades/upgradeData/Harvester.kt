package me.senseiju.sennetidle.upgrades.upgradeData

import me.senseiju.sennetidle.idlemob.DamageModifier
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.randomChance
import org.bukkit.Material

object Harvester : PlayerUpgrade {
    private const val chancePerLevel = 1.5F

    override val name  = "Harvester"
    override val material = Material.GOLDEN_HOE
    override val modelData = 0
    override val description = "Chance to drop extra reagents on hit"
    override val maxLevel = 10

    override fun onPlayerDamageIdleMob(user: User, dm: DamageModifier) {
        if (randomChance(user.getUpgradeLevel(Upgrade.HARVESTER) * chancePerLevel)) {

        }
    }
}