package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.user.User
import org.bukkit.Material

object Fireball : BasePower {
    override val promotionUnlock = 1
    override val cooldown = 45
    override val slot = 2
    override val name = "Fireball"
    override val description = "Burst damage"
    override val material = Material.FIRE_CHARGE
    override val modelData = 0

    override fun onRightClick(user: User) {
        TODO("Not yet implemented")
    }
}