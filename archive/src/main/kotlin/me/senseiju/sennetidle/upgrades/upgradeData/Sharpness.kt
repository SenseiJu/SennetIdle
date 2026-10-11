package me.senseiju.sennetidle.upgrades.upgradeData

import me.senseiju.sennetidle.idlemob.DamageModifier
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.user.User
import org.bukkit.Material

object Sharpness : PlayerUpgrade {
    private const val sharpnessDamageMultiplier = 0.05

    override val name  = "Sharpness"
    override val material = Material.GOLDEN_SWORD
    override val modelData = 0
    override val description = "Increases your click damage [$sharpnessDamageMultiplier% per level]"
    override val maxLevel = 25

    override fun onPlayerDamageIdleMob(user: User, dm: DamageModifier) {
        user.getUpgradeLevel(Upgrade.SHARPNESS).let {
            dm.finalDamage *= (1 + (it * sharpnessDamageMultiplier))
        }
    }
}