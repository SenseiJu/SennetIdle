package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.ModelItem

interface BasePower : ModelItem {
    val promotionUnlock: Int
    val description: String
    val cooldown: Int
    val slot: Int

    fun onRightClick(user: User)
}