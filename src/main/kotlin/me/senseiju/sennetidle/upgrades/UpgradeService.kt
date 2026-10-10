package me.senseiju.sennetidle.upgrades

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.utils.Service

class UpgradeService : Service() {

    init {
        plugin.commandManager.registerCommand(UpgradeCommand())
    }
}
