package gg.idlemob.idlemobcore.resource

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import gg.idlemob.idlemobcore.framework.extensions.parseSuffixed
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.plugin
import gg.idlemob.idlemobcore.resource.menu.ResourceListMenu
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("resource|resources")
class ResourceCommand(private val resources: ResourceRegistry, private val menu: ResourceListMenu) : BaseCommand() {
    private val messages
        get() = plugin.messages.resources

    @Default
    fun open(player: Player) {
        menu.open(player)
    }

    @Subcommand("give")
    @CommandPermission("idlemob.admin.resources")
    @CommandCompletion("@players @resources @nothing")
    fun give(sender: CommandSender, target: OnlinePlayer, id: String, amountInput: String) {
        val resource = resources[id] ?: return sender.sendMessage(messages.unknownResource(id))
        val amount = amountInput.parseSuffixed()?.takeIf { it > 0 && it.isFinite() }
            ?: return sender.sendMessage(messages.invalidAmount(amountInput))
        val user = plugin.users[target.player.uniqueId] ?: return sender.sendMessage(messages.userNotLoaded(target.player.name))

        user.addResource(id, amount)
        sender.sendMessage(messages.given(amount, resource.display.name, target.player.name))
        EventLogger.info("resource_given", target.player) {
            "resource" to id
            "amount" to amount
            "by" to sender.name
        }
    }
}
