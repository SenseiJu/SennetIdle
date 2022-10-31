package me.senseiju.sennetidle.user

import me.senseiju.sennetidle.powers.Power
import java.util.EnumMap

interface PowerHolder {
    val powerCooldowns: EnumMap<Power, Long>

    /**
     * Sets the cooldown for a specific [Power]
     *
     * @param power the power
     */
    fun setCooldown(power: Power) {
        powerCooldowns[power] = System.currentTimeMillis()
    }

    /**
     * Checks if the [Power] is currently on a cooldown
     *
     * @param power the power
     *
     * @return true if on cooldown
     */
    fun isOnCooldown(power: Power): Boolean {
        return System.currentTimeMillis() - powerCooldowns.getOrDefault(power, 0L) <= power.data.cooldown
    }

    /**
     * Gets the time remaining for a [Power] to be off cooldown
     *
     * @param power the power
     *
     * @return the time remaining in millis
     */
    fun getCooldownRemaining(power: Power): Long {
        return power.data.cooldown - (System.currentTimeMillis() - powerCooldowns.getOrDefault(power, 0))
    }

    fun tryActivate(power: Power): Boolean {
        if (isOnCooldown(power)) {
            return false
        }
        setCooldown(power)

        return true
    }
}