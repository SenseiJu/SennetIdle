package me.senseiju.sennetidle.reagents

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.utils.Service

class ReagentService : Service() {

    init {
        plugin.commandManager.registerCommand(ReagentCommand())
    }
}
