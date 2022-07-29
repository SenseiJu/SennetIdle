package me.senseiju.sennetidle.reagents

import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.service.Service

class ReagentService : Service() {

    init {
        plugin.commandManager.register(ReagentCommand())
    }

    /*
    private fun copyReagentsFromJar() {
        val jar = JarFile(plugin.javaClass.protectionDomain.codeSource.location.toURI().path)
        val entries = jar.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()

            if (!entry.name.startsWith("reagents/")) {
                continue
            }

            if (!File(plugin.dataFolder, entry.name).exists()) {
                plugin.saveResource(entry.name, false)
            }
        }
    }

     */

    /*
    private fun loadReagents() {
        val newReagents = HashMap<String, ReagentDef>()

        val json = Json { ignoreUnknownKeys = true }

        File(plugin.dataFolder, "reagents/")
            .walk()
            .filter { it.isFile }
            .forEach { reagentFile ->
                val reagentJson = reagentFile.readText()

                val reagentDef = try {
                    json.decodeFromString<ReagentDef>(reagentJson)
                } catch (ex: Exception) {
                    logger.error("Reagent failed to decode. File: ${reagentFile.name}")
                    ex.printStackTrace()
                    return@forEach
                }

                if (newReagents.containsKey(reagentDef.id)) {
                    logger.error("Reagent failed to load as it already exists. ReagentId: ${reagentDef.id}, File: ${reagentFile.name}")
                    return@forEach
                }

                newReagents[reagentDef.id] = reagentDef
            }

        reagents = newReagents
    }

     */
}