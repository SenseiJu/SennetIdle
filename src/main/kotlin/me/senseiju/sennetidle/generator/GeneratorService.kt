package me.senseiju.sennetidle.generator

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.generator.commands.GeneratorCommand
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import org.bukkit.Location
import java.io.File
import java.io.Serial

class GeneratorService(
    private val plugin: SennetIdle
) : Service() {
    private val generatorFile = File(plugin.dataFolder, "generators.json")

    val generators = hashMapOf<String, CraftingGenerator>()

    init {
        plugin.commandManager.register(GeneratorCommand(this))
        plugin.registerEvents(GeneratorListener(this))

        loadGenerators()
    }

    override fun onDisable() {
        saveGenerators()
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
        generators[id] = CraftingGenerator(plugin, id, location)
    }

    fun doesGeneratorExistsWithId(id: String) = generators.containsKey(id)

    fun getGeneratorByLocation(location: Location) = generators.values.firstOrNull { generator -> generator.location == location }
}