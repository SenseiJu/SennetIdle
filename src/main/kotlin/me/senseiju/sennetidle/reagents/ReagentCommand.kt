package me.senseiju.sennetidle.reagents

import me.mattstudios.mf.annotations.Alias
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import org.bukkit.entity.Player

@Command("Reagent")
@Alias("Reagents")
class ReagentCommand : CommandBase() {

    @Default
    fun default(player: Player) {
        player.openReagentGui()
    }
}