package me.senseiju.sennetidle.reagents

import me.mattstudios.mf.annotations.*
import me.mattstudios.mf.base.CommandBase
import me.senseiju.sennetidle.*
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.miniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

@Command("reagent")
@Alias("reagents")
class ReagentCommand : CommandBase() {

    @Default
    @Permission(PERMISSION_REAGENT_GUI)
    fun default(sender: Player) {
        sender.openReagentGui()
    }

    @SubCommand("craft")
    @Permission(PERMISSION_REAGENT_GUI)
    fun craft(sender: Player, @Completion("#enum") reagent: Reagent?) {
        if (reagent == null) {
            sender.miniMessage(Message.INVALID_REAGENT)
            return
        }

        sender.openCraftReagentGui(userService.getUser(sender.uniqueId), reagent)
    }

    @SubCommand("give")
    @Permission(PERMISSION_REAGENT_MODIFY)
    fun give(
        sender: CommandSender,
        @Completion("#players") target: Player?,
        @Completion("#enum") reagent: Reagent?,
        amount: Int?
    ) {
        if (target == null) {
            sender.miniMessage(Message.INVALID_PLAYER)
            return
        }

        if (reagent == null) {
            sender.miniMessage(Message.INVALID_REAGENT)
            return
        }

        if (amount == null) {
            sender.miniMessage(Message.INVALID_AMOUNT)
            return
        }

        userService.getUser(target.uniqueId).addReagent(reagent, amount)
        sender.miniMessage(
            Message.REAGENT_ADDED,
            Placeholder.unparsed("amount", "$amount"),
            Placeholder.unparsed("reagent", reagent.data.name),
            Placeholder.unparsed("player_name", target.name)
        )
    }

}