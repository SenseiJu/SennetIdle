package me.senseiju.sennetidle.commands

import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Optional
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.base.CommandBase
import me.senseiju.sennetidle.PERMISSION_DEV
import me.senseiju.sennetidle.PERMISSION_RELOAD
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.regents.RegentService
import me.senseiju.sennetidle.regents.openRegentsGui
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserService
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command("SennetIdle")
class SennetIdleCommand(plugin: SennetIdle) : CommandBase() {
    private val logger = plugin.slF4JLogger

    private val userService = serviceProvider.get<UserService>()
    private val regentService = serviceProvider.get<RegentService>()

    @SubCommand("Reload")
    @Permission(PERMISSION_RELOAD)
    fun reload(sender: CommandSender) {
        serviceProvider.reloadAll()
    }

    @SubCommand("OpenRegentsGui")
    @Permission(PERMISSION_RELOAD)
    fun openRegentsGui(sender: Player) {
        val user = userService.users[sender.uniqueId] ?: return

        sender.openRegentsGui(user)
    }

    @SubCommand("AddRegent")
    @Permission(PERMISSION_DEV)
    fun addRegent(sender: Player, target: Player, regentId: String, @Optional amount: Int? = 1) {
        amount ?: return
        if (!regentService.regents.containsKey(regentId)) return

        val user = userService.users[target.uniqueId] ?: return
        user.regents[regentId]?.let { it.amount += amount }

        logger.info("Added $amount $regentId to ${target.name}")
    }

    @SubCommand("ForceSave")
    @Permission(PERMISSION_DEV)
    fun forceSave(sender: Player) {
        userService.saveUsers()
    }
}