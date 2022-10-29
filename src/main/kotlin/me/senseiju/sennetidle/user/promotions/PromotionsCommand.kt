package me.senseiju.sennetidle.user.promotions

import me.mattstudios.mf.annotations.Alias
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.UserService
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

@Command("promotions")
@Alias("promotion", "promote")
class PromotionsCommand : CommandBase() {

    @Default
    fun default(sender: Player) {
        sender.openPromotionsGui(userService.getUser(sender.uniqueId))
    }
}