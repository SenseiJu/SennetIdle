package gg.idlemob.idlemobcore.box

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("boxes|box")
class BoxCommand(private val boxes: BoxRegistry, private val manager: BoxManager) : BaseCommand() {
    private val messages
        get() = plugin.messages.boxes

    @Subcommand("merge")
    fun merge(player: Player) {
        manager.merge(player)
    }

    @Subcommand("give")
    @CommandPermission("idlemob.admin.boxes")
    @CommandCompletion("@players @boxes")
    fun give(sender: CommandSender, target: OnlinePlayer, id: String, @Default("1") amount: Int) {
        val box = boxes[id]
        if (box == null) {
            sender.sendMessage(messages.unknownBox(id))
            return
        }

        if (amount !in 1..MAX_GIVE) {
            sender.sendMessage(messages.invalidAmount(MAX_GIVE))
            return
        }

        manager.give(target.player, id, amount)
        sender.sendMessage(messages.given(amount, box.display.name, target.player.name))
        target.player.sendMessage(messages.received(amount, box.display.name))
        EventLogger.info("box_given", target.player) {
            "box" to id
            "amount" to amount
            "by" to sender.name
        }
    }

    private companion object {
        const val MAX_GIVE = 2304
    }
}
