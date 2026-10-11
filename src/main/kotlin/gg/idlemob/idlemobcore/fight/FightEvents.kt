package gg.idlemob.idlemobcore.fight

import gg.idlemob.idlemobcore.user.User
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MobKilledEvent(
    val player: Player,
    val user: User,
    val stage: Int,
    val wave: Int,
    val boss: Boolean,
    val drops: Map<String, Double>,
) : Event() {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

class StageCompletedEvent(val player: Player, val user: User, val stage: Int) : Event() {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

class BossFailedEvent(val player: Player, val user: User, val stage: Int) : Event() {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
