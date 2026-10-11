package me.senseiju.sennetidle.upgrades.upgradeData

import me.senseiju.sennetidle.idlemob.DamageModifier
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.randomChance
import org.bukkit.Material

object Critical : PlayerUpgrade {
    private const val chancePerLevel = 1.5F

    override val name  = "Critical"
    override val material = Material.GOLDEN_AXE
    override val modelData = 0
    override val description = "Chance to double your click damage"
    override val maxLevel = 10

    override fun onPlayerDamageIdleMob(user: User, dm: DamageModifier) {
        if (randomChance(user.getUpgradeLevel(Upgrade.CRITICAL) * chancePerLevel)) {
            dm.finalDamage *= 2
        }
    }
}