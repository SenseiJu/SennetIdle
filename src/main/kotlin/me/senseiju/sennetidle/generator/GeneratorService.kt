package me.senseiju.sennetidle.generator

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.generator.commands.GeneratorCommand
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import org.bukkit.Location

class GeneratorService(
    private val plugin: SennetIdle
) : Service() {
    private val generators = hashMapOf<String, CraftingGenerator>()

    init {
        plugin.commandManager.register(GeneratorCommand(this))
        plugin.registerEvents(GeneratorListener(this))
    }

    fun createNewGenerator(id: String, location: Location) {
        generators[id] = CraftingGenerator(plugin, id, location)
    }

    fun doesGeneratorExistsWithId(id: String) = generators.containsKey(id)

    fun getGeneratorByLocation(location: Location) = generators.values.firstOrNull { generator -> generator.location == location }
}