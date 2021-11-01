package me.senseiju.sennetidle

import me.mattstudios.mf.base.CommandManager
import me.senseiju.sennetidle.commands.SennetIdleCommand
import me.senseiju.sennetidle.crafting.CraftingService
import me.senseiju.sennetidle.database.Database
import me.senseiju.sennetidle.idlemobs.IdleMobService
import me.senseiju.sennetidle.inventory.InventoryService
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.users.UserService
import me.senseiju.sentils.service.ServiceProvider
import me.senseiju.sentils.storage.ConfigFile
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.Style
import org.bukkit.GameRule
import org.bukkit.plugin.java.JavaPlugin

val serviceProvider = ServiceProvider()

lateinit var database: Database
    private set

class SennetIdle : JavaPlugin() {
    lateinit var commandManager: CommandManager

    override fun onEnable() {
        commandManager = CommandManager(this)

        try {
            establishDatabase()
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

        SennetIdlePlaceholders().register()
    }

    override fun onDisable() {
        serviceProvider.disableAll()

        server.onlinePlayers.forEach {
            it.kick(text("SennetIdle is restarting...", Style.empty()))
        }
    }

    private fun enableServices() {
        serviceProvider.add(IdleMobService(this))
        serviceProvider.add(ReagentService(this))
        serviceProvider.add(CraftingService(this))
        serviceProvider.add(UserService(this))
        serviceProvider.add(InventoryService(this))
    }

    private fun registerCommands() {
        commandManager.register(
            SennetIdleCommand(this)
        )
    }

    /**
     * Creates connection to database and relevant tables
     */
    private fun establishDatabase() {
        database = Database(ConfigFile(this, "database.yml", true))
    }
}