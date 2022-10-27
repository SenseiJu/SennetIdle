package me.senseiju.sennetidle.upgrades.upgradeData

import me.senseiju.sennetidle.idlemob.DamageModifier
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.ModelItem

interface BaseUpgrade : ModelItem {
    val description: String
    val maxLevel: Int
}

interface PlayerUpgrade : BaseUpgrade {
    fun onPlayerDamageIdleMob(user: User, dm: DamageModifier) { }
}

interface IdleMobUpgrade: BaseUpgrade