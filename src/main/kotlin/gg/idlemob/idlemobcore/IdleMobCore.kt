package gg.idlemob.idlemobcore

import co.aikar.commands.PaperCommandManager
import gg.idlemob.idlemobcore.admin.IdleAdminCommand
import gg.idlemob.idlemobcore.blueprint.BlueprintCommand
import gg.idlemob.idlemobcore.blueprint.BlueprintItems
import gg.idlemob.idlemobcore.blueprint.Blueprints
import gg.idlemob.idlemobcore.box.BoxCommand
import gg.idlemob.idlemobcore.box.BoxManager
import gg.idlemob.idlemobcore.box.BoxRegistry
import gg.idlemob.idlemobcore.economy.BalanceCommand
import gg.idlemob.idlemobcore.economy.BaltopCommand
import gg.idlemob.idlemobcore.economy.Currency
import gg.idlemob.idlemobcore.economy.EcoCommand
import gg.idlemob.idlemobcore.economy.EconomyService
import gg.idlemob.idlemobcore.economy.PayCommand
import gg.idlemob.idlemobcore.fight.DropRoller
import gg.idlemob.idlemobcore.fight.FightManager
import gg.idlemob.idlemobcore.fight.StashRestore
import gg.idlemob.idlemobcore.framework.config.ConfigFile
import gg.idlemob.idlemobcore.framework.content.ContentProblem
import gg.idlemob.idlemobcore.framework.coroutines.PluginScope
import gg.idlemob.idlemobcore.framework.database.Database
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.generator.GeneratorCommand
import gg.idlemob.idlemobcore.generator.GeneratorManager
import gg.idlemob.idlemobcore.generator.GeneratorRepository
import gg.idlemob.idlemobcore.generator.menu.GeneratorMenu
import gg.idlemob.idlemobcore.placeholder.IdleMobExpansion
import gg.idlemob.idlemobcore.prestige.PrestigeCommand
import gg.idlemob.idlemobcore.prestige.PrestigeRegistry
import gg.idlemob.idlemobcore.prestige.PrestigeService
import gg.idlemob.idlemobcore.prestige.UpgradeCommand
import gg.idlemob.idlemobcore.prestige.UpgradeRegistry
import gg.idlemob.idlemobcore.prestige.UpgradeStatProvider
import gg.idlemob.idlemobcore.prestige.menu.PrestigeMenu
import gg.idlemob.idlemobcore.prestige.menu.UpgradeMenu
import gg.idlemob.idlemobcore.resource.ResourceCommand
import gg.idlemob.idlemobcore.resource.ResourceCrafter
import gg.idlemob.idlemobcore.resource.menu.ResourceListMenu
import gg.idlemob.idlemobcore.resource.ResourceRegistry
import gg.idlemob.idlemobcore.resource.ResourceStatProvider
import gg.idlemob.idlemobcore.stats.StatService
import gg.idlemob.idlemobcore.user.UserManager
import gg.idlemob.idlemobcore.user.UserRepository
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI

lateinit var plugin: IdleMobCore
    private set

class IdleMobCore : JavaPlugin() {
    private lateinit var configFile: ConfigFile<Config>
    private lateinit var messagesFile: ConfigFile<Messages>

    lateinit var scope: PluginScope
        private set
    lateinit var database: Database
        private set
    lateinit var resources: ResourceRegistry
        private set
    lateinit var blueprints: Blueprints
        private set
    lateinit var users: UserManager
        private set
    lateinit var stats: StatService
        private set
    lateinit var fight: FightManager
        private set
    lateinit var boxes: BoxRegistry
        private set
    lateinit var boxManager: BoxManager
        private set
    lateinit var economy: EconomyService
        private set
    lateinit var prestiges: PrestigeRegistry
        private set
    lateinit var upgrades: UpgradeRegistry
        private set
    lateinit var prestige: PrestigeService
        private set
    lateinit var generators: GeneratorManager
        private set
    lateinit var generatorMenu: GeneratorMenu
        private set
    lateinit var commandManager: PaperCommandManager
        private set
    private var placeholders: IdleMobExpansion? = null

    val cfg: Config
        get() = configFile.config
    val messages: Messages
        get() = messagesFile.config

    init {
        plugin = this
    }

    override fun onEnable() {
        EventLogger.logger = slF4JLogger

        try {
            doEnable()
        } catch (ex: Exception) {
            EventLogger.error("startup_failed", ex)
            server.shutdown()
        }
    }

    private fun doEnable() {
        InvUI.getInstance().setPlugin(this)

        configFile = ConfigFile(dataPath.resolve("config.yml"))
        messagesFile = ConfigFile(dataPath.resolve("messages.yml"))

        scope = PluginScope(this)

        resources = ResourceRegistry(dataPath.resolve("resources"))
        blueprints = Blueprints(resources)
        boxes = BoxRegistry(dataPath.resolve("boxes"))
        prestiges = PrestigeRegistry(dataPath.resolve("prestiges"), resources)
        upgrades = UpgradeRegistry(dataPath.resolve("upgrades"))
        reloadContent()

        database = Database(cfg.database)
        database.migrate()

        val userRepository = UserRepository(database)
        users = UserManager(this, scope, userRepository)
        economy = EconomyService(database, users)

        stats = StatService()
        stats.register(ResourceStatProvider(resources))
        stats.register(UpgradeStatProvider(upgrades))

        fight = FightManager(this, users, stats, DropRoller(resources))
        StashRestore(this)
        generators = GeneratorManager(this, blueprints, resources, GeneratorRepository(database), BlueprintItems(this, blueprints))
        generatorMenu = GeneratorMenu(generators)
        boxManager = BoxManager(this, boxes)
        prestige = PrestigeService(this, prestiges, upgrades, database, userRepository, economy)
        val upgradeMenu = UpgradeMenu(upgrades, prestige)

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            placeholders = IdleMobExpansion(this).also { it.register() }
        }

        commandManager = PaperCommandManager(this)
        commandManager.commandCompletions.registerCompletion("boxes") { boxes.entries.keys }
        commandManager.commandCompletions.registerCompletion("resources") { resources.entries.keys }
        commandManager.commandCompletions.registerCompletion("blueprints") { blueprints.ids }
        commandManager.commandCompletions.registerCompletion("currencies") { Currency.entries.map { it.id } }
        commandManager.registerCommand(IdleAdminCommand())
        commandManager.registerCommand(BoxCommand(boxes, boxManager))
        commandManager.registerCommand(ResourceCommand(resources, ResourceListMenu(resources, ResourceCrafter(resources))))
        commandManager.registerCommand(PrestigeCommand(PrestigeMenu(prestige, upgradeMenu)))
        commandManager.registerCommand(UpgradeCommand(upgradeMenu))
        commandManager.registerCommand(GeneratorCommand(generatorMenu))
        commandManager.registerCommand(BlueprintCommand(blueprints, generators.items))
        commandManager.registerCommand(BalanceCommand(economy))
        commandManager.registerCommand(PayCommand(economy))
        commandManager.registerCommand(BaltopCommand(economy))
        commandManager.registerCommand(EcoCommand(economy))
    }

    override fun onDisable() {
        placeholders?.unregister()

        if (::fight.isInitialized) {
            fight.shutdown()
        }

        if (::generators.isInitialized) {
            generators.shutdown()
        }

        if (::users.isInitialized) {
            users.shutdown()
        }

        if (::scope.isInitialized) {
            scope.shutdown()
        }

        if (::database.isInitialized) {
            database.close()
        }
    }

    fun reloadConfigs(): ReloadReport {
        configFile.reload()
        messagesFile.reload()
        return reloadContent()
    }

    private fun reloadContent(): ReloadReport {
        val problems = resources.reload() + boxes.reload() + prestiges.reload() + upgrades.reload()
        return ReloadReport(resources.entries.size, blueprints.ids.size, boxes.entries.size, problems)
    }
}

class ReloadReport(val resources: Int, val blueprints: Int, val boxes: Int, val problems: List<ContentProblem>)
