package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.user.User
import org.bukkit.Material

object AdrenalineRush : BasePower {
    override val promotionUnlock = 3
    override val cooldown = 120
    override val slot = 3
    override val name = "Adrenaline Rush"
    override val description = "Fast"
    override val material = Material.GOLDEN_BOOTS
    override val modelData = 0

    override fun onRightClick(user: User) {
        TODO("Not yet implemented")
    }
}