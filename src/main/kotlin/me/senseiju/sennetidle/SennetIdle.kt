package me.senseiju.sennetidle

import me.mattstudios.mf.base.CommandManager
import me.senseiju.sennetidle.idlemob.IdleMobService
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.storage.StorageService
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sentils.service.ServiceProvider
import me.senseiju.sentils.storage.ConfigFile
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.Style
import org.bukkit.GameRule
import org.bukkit.plugin.java.JavaPlugin

val plugin = JavaPlugin.getPlugin(SennetIdle::class.java)
val serviceProvider = ServiceProvider()

class SennetIdle : JavaPlugin() {
    lateinit var config: ConfigFile
    lateinit var commandManager: CommandManager

    override fun onEnable() {
        config = ConfigFile(this, "config.yml", true)
        commandManager = CommandManager(this)

        try {
            enableServices()
            registerCommands()
        } catch (ex: Exception) {
            slF4JLogger.error("Failed to load a service. Check stack trace for more information. SHUTTING DOWN")
            ex.printStackTrace()
            server.shutdown()
            return
        }

        server.worlds.forEach {
            it.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)
            it.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            it.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            it.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            it.setGameRule(GameRule.DO_MOB_LOOT, false)
        }
    }

    override fun onDisable() {
        serviceProvider.disableAll()

        server.onlinePlayers.forEach {
            it.kick(text("SennetIdle is restarting...", Style.empty()))
        }
    }

    private fun enableServices() {
        serviceProvider.add(StorageService())
        serviceProvider.add(UserService())
        serviceProvider.add(ReagentService())
        serviceProvider.add(IdleMobService())
    }

    private fun registerCommands() {
        commandManager.register(

        )
    }
}