package gg.idlemob.idlemobcore.prestige

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import gg.idlemob.idlemobcore.prestige.menu.PrestigeMenu
import gg.idlemob.idlemobcore.prestige.menu.UpgradeMenu
import org.bukkit.entity.Player

@CommandAlias("prestige|promote")
class PrestigeCommand(private val menu: PrestigeMenu) : BaseCommand() {

    @Default
    fun open(player: Player) {
        menu.open(player)
    }
}

@CommandAlias("upgrades|upgrade")
class UpgradeCommand(private val menu: UpgradeMenu) : BaseCommand() {

    @Default
    fun open(player: Player) {
        menu.open(player)
    }
}
