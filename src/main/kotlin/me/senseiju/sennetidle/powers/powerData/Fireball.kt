package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.powers.Power
import me.senseiju.sennetidle.user.User
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import kotlin.math.roundToLong

object Fireball : BasePower {
    override val promotionUnlock = 1
    override val cooldown = 45
    override val slot = 1
    override val name = "Fireball"
    override val description = "Burst damage"
    override val material = Material.FIRE_CHARGE
    override val modelData = 0

    override fun onActivate(user: User) {
        user.idleMob?.let {
            if (!isReady(user, Power.FIREBALL)) {
                user.message(
                    Message.POWER_COOLDOWN_REMAINING,
                    Placeholder.unparsed("power", name),
                    Placeholder.unparsed("time_remaining", "${cooldown - (timeRemaining(user, Power.FIREBALL) / 1000)}")
                )
                return
            }
            setCooldown(user, Power.FIREBALL)

            it.applyDamage((it.maxHealth * 0.1).roundToLong())
        }
    }
}