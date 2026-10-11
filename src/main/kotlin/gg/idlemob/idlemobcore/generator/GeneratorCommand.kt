package gg.idlemob.idlemobcore.generator

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import gg.idlemob.idlemobcore.generator.menu.GeneratorMenu
import org.bukkit.entity.Player

@CommandAlias("generators|generator|gens")
class GeneratorCommand(private val menu: GeneratorMenu) : BaseCommand() {

    @Default
    fun open(player: Player) {
        menu.open(player)
    }
}
