package me.senseiju.sennetidle.upgrades

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

@CommandAlias("upgrade|upgrades")
class UpgradeCommand : BaseCommand() {

    @Default
    @CommandPermission(PERMISSION_UPGRADE_GUI)
    fun default(sender: Player) {
        sender.openUpgradeGui()
    }

    @Subcommand("bal|balance")
    fun balance(sender: Player) {
        val user = userService.getUser(sender.uniqueId)

        sender.message(
            Message.UPGRADE_POINTS_TO_SPEND,
            Placeholder.unparsed("unspent_points", "${user.unspentUpgradePoints}")
        )
    }

    @Subcommand("addUpgradePoints")
    @CommandPermission(PERMISSION_UPGRADE_MODIFY)
    @CommandCompletion("@players @nothing")
    @Syntax("<player> <amount>")
    fun addUpgradePoints(sender: CommandSender, target: OnlinePlayer, amount: Int) {
        val player = target.player

        userService.getUser(player.uniqueId).apply {
            unspentUpgradePoints = (unspentUpgradePoints + amount).coerceAtLeast(0)
        }
        sender.message(
            Message.UPGRADE_POINTS_ADDED,
            Placeholder.unparsed("amount", "$amount"),
            Placeholder.unparsed("player_name", player.name)
        )
    }

    @Subcommand("set")
    @CommandPermission(PERMISSION_UPGRADE_MODIFY)
    @CommandCompletion("@players @upgrades @nothing")
    @Syntax("<player> <upgrade> <level>")
    fun set(sender: CommandSender, target: OnlinePlayer, upgrade: Upgrade, level: Int) {
        userService.getUser(target.player.uniqueId).let {
            it.upgrades[upgrade] = level.coerceIn(0, upgrade.data.maxLevel)
        }
    }
}
