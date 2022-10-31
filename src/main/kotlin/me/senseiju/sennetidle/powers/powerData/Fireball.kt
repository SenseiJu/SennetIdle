package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.powers.Power
import me.senseiju.sennetidle.user.User
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

private val power = Power.FIREBALL

object Fireball : BasePower {
    private const val DAMAGE_MULTIPLIER = 0.1

    override val promotionUnlock = 1
    override val cooldown = TimeUnit.SECONDS.toMillis(45)
    override val slot = 1
    override val name = "Fireball"
    override val description = "Burst damage"
    override val material = Material.FIRE_CHARGE
    override val modelData = 0

    override fun onRightClick(user: User) {
        val idleMob = user.idleMob ?: return

        if (!user.tryActivate(power)) {
            user.messageCooldown(power)
            return
        }
        user.message(Message.POWER_ACTIVATED, Placeholder.unparsed("power", name))

        idleMob.applyDamage((idleMob.maxHealth * DAMAGE_MULTIPLIER).roundToLong())
    }
}