package gg.idlemob.idlemobcore.blueprint

import gg.idlemob.idlemobcore.framework.extensions.giveOrDrop
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.plugin
import org.bukkit.command.CommandSender

@CommandAlias("blueprint|blueprints")
class BlueprintCommand(private val blueprints: Blueprints, private val items: BlueprintItems) : BaseCommand() {
    private val messages
        get() = plugin.messages.generators

    @Subcommand("give")
    @CommandPermission("idlemob.admin.blueprints")
    @CommandCompletion("@players @blueprints @nothing")
    fun give(sender: CommandSender, target: OnlinePlayer, id: String, uses: Int) {
        val blueprint = blueprints[id] ?: return sender.sendMessage(messages.unknownBlueprint(id))
        if (uses !in 1..MAX_USES) {
            sender.sendMessage(messages.invalidUses(MAX_USES))
            return
        }

        val item = items.create(id, uses)
        target.player.giveOrDrop(listOf(item))
        sender.sendMessage(messages.blueprintGiven(blueprint.name, uses, target.player.name))
        EventLogger.info("blueprint_given", target.player) {
            "blueprint" to id
            "uses" to uses
            "blueprint_uuid" to items.read(item)?.uuid
            "by" to sender.name
        }
    }

    private companion object {
        const val MAX_USES = 1_000_000
    }
}
