package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.upgrades.Upgrade
import java.util.*

interface UpgradeHolder {
    val upgrades: EnumMap<Upgrade, Int>

    fun getUpgradeLevel(upgrade: Upgrade): Int {
        return upgrades.getOrPut(upgrade) { 0 }
    }

    fun addUpgradeLevel(upgrade: Upgrade, level: Int) {
        upgrades.putIfAbsent(upgrade, 0)
        upgrades[upgrade] = upgrades[upgrade]!! + level
    }

    fun canLevelUpgrade(upgrade: Upgrade): Boolean {
        return getUpgradeLevel(upgrade) < upgrade.data.maxLevel
    }
}