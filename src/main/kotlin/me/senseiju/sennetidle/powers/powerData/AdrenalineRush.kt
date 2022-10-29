package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.powers.Power
import me.senseiju.sennetidle.user.User
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material

object AdrenalineRush : BasePower {
    val duration = 5
    override val promotionUnlock = 3
    override val cooldown = 120 + duration
    override val slot = 2
    override val name = "Adrenaline Rush"
    override val description = "Fast"
    override val material = Material.GOLDEN_BOOTS
    override val modelData = 0


    override fun onActivate(user: User) {
        if (!isReady(user, Power.ADRENALINE_RUSH)) {
            user.message(
                Message.POWER_COOLDOWN_REMAINING,
                Placeholder.unparsed("power", name),
                Placeholder.unparsed("time_remaining", "${cooldown - (timeRemaining(user, Power.ADRENALINE_RUSH) / 1000)}")
            )
            return
            }
        setCooldown(user, Power.ADRENALINE_RUSH)
        }
    }

