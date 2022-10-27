package me.senseiju.sennetidle.user

import me.mattstudios.mf.annotations.Alias
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import me.senseiju.sennetidle.idlemob.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()
private val idleMobService = serviceProvider.get<IdleMobService>()

@Command("promote")
@Alias("promotion", "promotions")
class PromoteCommand : CommandBase() {

    @Default
    fun default(sender: Player) {
        val user = userService.getUser(sender.uniqueId)

        if (user.canPromote()) {
            user.currentWave = 1
            user.promotions++
            user.unspentUpgradePoints += 3

            idleMobService.forceNewIdleMob(user)
        }
    }
}