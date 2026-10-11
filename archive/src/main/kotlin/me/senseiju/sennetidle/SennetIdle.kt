package me.senseiju.sennetidle

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import me.senseiju.sennetidle.config.Config
import me.senseiju.sennetidle.config.ConfigLoader
import me.senseiju.sennetidle.idlemob.IdleMobService
import me.senseiju.sennetidle.npcs.NpcInteractListener
import me.senseiju.sennetidle.powers.PowerService
import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.storage.StorageService
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.upgrades.UpgradeService
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.ServiceProvider
import me.senseiju.sennetidle.utils.extensions.register
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.Style
import org.bukkit.GameRules
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI

lateinit var plugin: SennetIdle
    private set
val serviceProvider = ServiceProvider()

class SennetIdle : JavaPlugin() {
    private lateinit var configLoader: ConfigLoader<Config>
    lateinit var commandManager: PaperCommandManager
        private set

    val cfg: Config
        get() = configLoader.config

    init {
        plugin = this
    }

    override fun onEnable() {
        InvUI.getInstance().setPlugin(this)

        try {
            configLoader = ConfigLoader(dataPath, "config")
            setupCommandManager()
            enableServices()
        } catch (ex: Exception) {
            slF4JLogger.error("Failed to load a service. Check stack trace for more information. SHUTTING DOWN", ex)
            server.shutdown()
            return
        }

        NpcInteractListener().register()

        server.worlds.forEach {
            it.setGameRule(GameRules.MAX_ENTITY_CRAMMING, 0)
            it.setGameRule(GameRules.ADVANCE_TIME, false)
            it.setGameRule(GameRules.ADVANCE_WEATHER, false)
            it.setGameRule(GameRules.SPAWN_MOBS, false)
            it.setGameRule(GameRules.MOB_DROPS, false)
        }
    }

    override fun onDisable() {
        serviceProvider.disableAll()

        server.onlinePlayers.forEach {
            it.kick(text("SennetIdle is restarting...", Style.empty()))
        }
    }

    private fun setupCommandManager() {
        commandManager = PaperCommandManager(this)

        commandManager.commandContexts.registerContext(Reagent::class.java) { ctx ->
            val arg = ctx.popFirstArg()
            Reagent.entries.firstOrNull { it.name.equals(arg, true) } ?: throw InvalidCommandArgument("Invalid reagent")
        }
        commandManager.commandCompletions.registerStaticCompletion("reagents", Reagent.entries.map { it.name.lowercase() })

        commandManager.commandContexts.registerContext(Upgrade::class.java) { ctx ->
            val arg = ctx.popFirstArg()
            Upgrade.entries.firstOrNull { it.name.equals(arg, true) } ?: throw InvalidCommandArgument("Invalid upgrade")
        }
        commandManager.commandCompletions.registerStaticCompletion("upgrades", Upgrade.entries.map { it.name.lowercase() })
    }

    private fun enableServices() {
        serviceProvider.add(StorageService())
        serviceProvider.add(UserService())
        serviceProvider.add(ReagentService())
        serviceProvider.add(IdleMobService())
        serviceProvider.add(UpgradeService())
        serviceProvider.add(PowerService())
    }
}
