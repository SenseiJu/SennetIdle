package me.senseiju.sennetidle.commands

import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Optional
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.base.CommandBase
import me.senseiju.sennetidle.PERMISSION_DEV
import me.senseiju.sennetidle.PERMISSION_RELOAD
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.reagents.openReagentsGui
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command("SennetIdle")
class SennetIdleCommand(plugin: SennetIdle) : CommandBase() {
    private val logger = plugin.slF4JLogger

    private val userService = serviceProvider.get<UserService>()
    private val regentService = serviceProvider.get<ReagentService>()

    @SubCommand("Reload")
    @Permission(PERMISSION_RELOAD)
    fun reload(sender: CommandSender) {
        serviceProvider.reloadAll()
    }

    @SubCommand("OpenRegentsGui")
    @Permission(PERMISSION_RELOAD)
    fun openRegentsGui(sender: Player) {
        val user = userService.getUser(sender.uniqueId)

        sender.openReagentsGui(user)
    }

    @SubCommand("AddRegent")
    @Permission(PERMISSION_DEV)
    fun addRegent(sender: Player, target: Player, regentId: String, @Optional amount: Int? = 1) {
        amount ?: return
        if (!regentService.reagents.containsKey(regentId)) return

        val user = userService.getUser(target.uniqueId)
        user.reagents[regentId]?.let { it.amount += amount }

        logger.info("Added $amount $regentId to ${target.name}")
    }

    @SubCommand("ForceSave")
    @Permission(PERMISSION_DEV)
    fun forceSave(sender: Player) {
        userService.saveUsers()
    }
}