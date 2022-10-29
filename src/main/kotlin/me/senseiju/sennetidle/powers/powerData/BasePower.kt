package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.powers.Power
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.ModelItem

interface BasePower : ModelItem {
    val promotionUnlock: Int
    val description: String
    val cooldown: Int
    val slot: Int

    fun onActivate(user: User)

    fun setCooldown(user: User, power: Power) {
        user.powerCooldowns[power] = System.currentTimeMillis()
    }

    fun isReady(user: User, power: Power): Boolean {
        return System.currentTimeMillis() - user.powerCooldowns.getOrDefault(power, 0) <= cooldown * 1000
    }

    fun timeRemaining(user: User, power: Power): Long {
        return cooldown * 1000 - (System.currentTimeMillis() - user.powerCooldowns.getOrDefault(power, 0))
    }
}
