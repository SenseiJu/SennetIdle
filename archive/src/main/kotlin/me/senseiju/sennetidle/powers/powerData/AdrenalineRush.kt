package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.powers.Power
import me.senseiju.sennetidle.user.User
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import java.util.concurrent.TimeUnit

private val power = Power.ADRENALINE_RUSH

object AdrenalineRush : BasePower {
    const val DAMAGE_MULTIPLIER = 1.15
    private val duration = TimeUnit.SECONDS.toMillis(5)

    override val promotionUnlock = 3
    override val cooldown = TimeUnit.SECONDS.toMillis(120) + duration
    override val slot = 2
    override val name = "Adrenaline Rush"
    override val description = "Fast"
    override val material = Material.GOLDEN_BOOTS
    override val modelData = 0


    override fun onRightClick(user: User) {
        if (!user.tryActivate(power)) {
            user.messageCooldown(power)
            return
        }
        user.message(Message.POWER_ACTIVATED_WITH_DURATION, Placeholder.unparsed("power", name), Placeholder.unparsed("duration", (duration / 1000).toString()))
    }

    fun isActive(user: User): Boolean {
        return cooldown - user.getCooldownRemaining(power) < duration
    }
}

