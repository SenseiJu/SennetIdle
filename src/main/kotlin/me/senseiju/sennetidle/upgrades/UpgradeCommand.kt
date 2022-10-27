package me.senseiju.sennetidle.upgrades

import me.mattstudios.mf.annotations.*
import me.mattstudios.mf.base.CommandBase
import me.senseiju.sennetidle.*
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.miniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player

private val userService = serviceProvider.get<UserService>()

@Command("upgrade")
@Alias("upgrades")
class UpgradeCommand : CommandBase() {

    @Default
    @Permission(PERMISSION_UPGRADE_GUI)
    fun default(sender: Player) {
        sender.openUpgradeGui()
    }

    @SubCommand("bal")
    @Alias("balance")
    fun balance(sender: Player) {
        val user = userService.getUser(sender.uniqueId)

        sender.miniMessage(
            Message.UPGRADE_POINTS_TO_SPEND,
            Placeholder.unparsed("unspent_points", "${user.unspentUpgradePoints}")
        )
    }

    @SubCommand("addUpgradePoints")
    @Permission(PERMISSION_UPGRADE_MODIFY)
    fun addUpgradePoints(sender: Player, @Completion("#players") target: Player?, amount: Int?) {
        if (target == null) {
            sender.miniMessage(Message.INVALID_PLAYER)
            return
        }

        if (amount == null) {
            sender.miniMessage(Message.INVALID_AMOUNT)
            return
        }

        userService.getUser(target.uniqueId).apply {
            unspentUpgradePoints = (unspentUpgradePoints + amount).coerceAtLeast(0)
        }
        sender.miniMessage(
            Message.UPGRADE_POINTS_ADDED,
            Placeholder.unparsed("amount", "$amount"),
            Placeholder.unparsed("player_name", target.name)
        )
    }

    @SubCommand("set")
    @Permission(PERMISSION_UPGRADE_MODIFY)
    fun set(sender: Player, @Completion("#players") target: Player?, @Completion("#enum") upgrade: Upgrade?, level: Int?) {
        if (target == null) {
            sender.miniMessage(Message.INVALID_PLAYER)
            return
        }

        if (upgrade == null) {
            sender.miniMessage(Message.INVALID_UPGRADE)
            return
        }

        if (level == null) {
            sender.miniMessage(Message.INVALID_LEVEL)
            return
        }

        userService.getUser(target.uniqueId).let {
            it.upgrades[upgrade] = level.coerceIn(0, upgrade.data.maxLevel)
        }
    }
}