package me.senseiju.sennetidle.generator

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.generator.commands.GeneratorCommand
import me.senseiju.sennetidle.generator.holograms.HologramHandler
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.users.UserService
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.runnables.newRunnable
import me.senseiju.sentils.service.Service
import org.bukkit.Location
import java.io.File
import java.util.concurrent.TimeUnit

private val userService = serviceProvider.get<UserService>()

class GeneratorService(
    private val plugin: SennetIdle
) : Service() {
    private val generatorFile = File(plugin.dataFolder, "generators.json")

    val hologramHandler = HologramHandler(plugin)
    val generators = hashMapOf<String, CraftingGenerator>()

    init {
        plugin.commandManager.register(GeneratorCommand(this))
        plugin.registerEvents(GeneratorListener(this))

        loadGenerators()
    }

    override fun onDisable() {
        saveGenerators()
        hologramHandler.deleteAllHolograms()
    }

    private fun loadGenerators() {
        if (!generatorFile.exists()) return

        val generatorList = Json.decodeFromString<List<String>>(generatorFile.readText()).map {
            Json.decodeFromString<SerializableCraftingGenerator>(it)
        }

        generatorList.forEach {
            if (!doesGeneratorExistsWithId(it.id)) {
                createNewGenerator(it.id, it.location)
            }
        }
    }

    private fun saveGenerators() {
        if (!generatorFile.exists()) {
            generatorFile.createNewFile()
        }

        val generatorList = generators.values.map { it.toJson() }

        generatorFile.writeText(Json.encodeToString(generatorList))
    }

    fun createNewGenerator(id: String, location: Location) {
        val generator = CraftingGenerator(plugin, id, location)

        generators[id] = generator
        hologramHandler.createHolograms(generator)
    }

    fun deleteGenerator(id: String) {
        val generator = generators.remove(id) ?: return

        generator.dispose()
    }

    fun doesGeneratorExistsWithId(id: String) = generators.containsKey(id)

    fun getGeneratorByLocation(location: Location) = generators.values.firstOrNull { generator -> generator.location == location }

    fun accelerateGenerators(user: User) {
        newRunnable {
            val logOutPeriod = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - user.lastSeen)

            generators.values.forEach {
                for (i in 0..logOutPeriod * 20) {
                    it.activeUserCrafts[user]?.run()
                }
            }
        }.runTaskAsynchronously(plugin)
    }
}