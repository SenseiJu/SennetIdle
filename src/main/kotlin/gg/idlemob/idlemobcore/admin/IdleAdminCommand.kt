package gg.idlemob.idlemobcore.admin

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.plugin
import org.bukkit.command.CommandSender

@CommandAlias("idleadmin")
class IdleAdminCommand : BaseCommand() {

    @Subcommand("reload")
    @CommandPermission("idlemob.admin.reload")
    fun reload(sender: CommandSender) {
        try {
            val report = plugin.reloadConfigs()
            sender.sendMessage(plugin.messages.admin.reloadSuccess(report.resources, report.blueprints, report.boxes))
            if (report.problems.isNotEmpty()) {
                sender.sendMessage(plugin.messages.admin.reloadProblems(report.problems.size))
            }
        } catch (ex: Exception) {
            EventLogger.error("config_reload_failed", ex)
            sender.sendMessage(plugin.messages.admin.reloadFailed(ex.message ?: "unknown error"))
        }
    }

    @Subcommand("debug")
    @CommandPermission("idlemob.admin.debug")
    @CommandCompletion("true|false")
    fun debug(sender: CommandSender, @Optional enabled: Boolean?) {
        EventLogger.debugEnabled = enabled ?: !EventLogger.debugEnabled
        sender.sendMessage(plugin.messages.admin.debugToggled(EventLogger.debugEnabled))
    }
}
