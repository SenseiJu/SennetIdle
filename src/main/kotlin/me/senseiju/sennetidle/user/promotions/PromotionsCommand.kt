package me.senseiju.sennetidle.user.promotions

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.UserService
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

@CommandAlias("promotions|promotion|promote")
class PromotionsCommand : BaseCommand() {

    @Default
    fun default(sender: Player) {
        sender.openPromotionsGui(userService.getUser(sender.uniqueId))
    }
}
