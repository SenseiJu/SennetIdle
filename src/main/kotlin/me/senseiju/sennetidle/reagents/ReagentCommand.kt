package me.senseiju.sennetidle.reagents

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.senseiju.sennetidle.*
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.message
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

@CommandAlias("reagent|reagents")
class ReagentCommand : BaseCommand() {

    @Default
    @CommandPermission(PERMISSION_REAGENT_GUI)
    fun default(sender: Player) {
        sender.openReagentGui()
    }

    @Subcommand("craft")
    @CommandPermission(PERMISSION_REAGENT_GUI)
    @CommandCompletion("@reagents")
    @Syntax("<reagent>")
    fun craft(sender: Player, reagent: Reagent) {
        sender.openCraftReagentGui(userService.getUser(sender.uniqueId), reagent)
    }

    @Subcommand("give")
    @CommandPermission(PERMISSION_REAGENT_MODIFY)
    @CommandCompletion("@players @reagents @nothing")
    @Syntax("<player> <reagent> <amount>")
    fun give(sender: CommandSender, target: OnlinePlayer, reagent: Reagent, amount: Int) {
        val player = target.player

        userService.getUser(player.uniqueId).addReagent(reagent, amount)
        sender.message(
            Message.REAGENT_ADDED,
            Placeholder.unparsed("amount", "$amount"),
            Placeholder.unparsed("reagent", reagent.data.name),
            Placeholder.unparsed("player_name", player.name)
        )
    }
}
