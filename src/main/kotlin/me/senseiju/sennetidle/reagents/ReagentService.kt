package me.senseiju.sennetidle.reagents

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sentils.service.Service
import java.io.File
import java.util.jar.JarFile

class ReagentService(private val plugin: SennetIdle) : Service() {
    private val logger = plugin.slF4JLogger

    var reagents = hashMapOf<String, Reagent>()
        private set

    init {
        copyReagentsFromJar()
        loadReagents()
    }

    override fun onReload() {
        loadReagents()
    }

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

    private fun loadReagents() {
        val newReagents = HashMap<String, Reagent>()

        File(plugin.dataFolder, "reagents/")
            .walk()
            .filter { it.isFile }
            .forEach { reagentFile ->
                val reagentJson = reagentFile.readText()

                val reagent = try {
                    Json.decodeFromString<Reagent>(reagentJson)
                } catch (ex: Exception) {
                    logger.error("Reagent failed to decode. File: ${reagentFile.name}")
                    ex.printStackTrace()
                    return@forEach
                }

                if (newReagents.containsKey(reagent.id)) {
                    logger.error("Reagent failed to load as it already exists. ReagentId: ${reagent.id}, File: ${reagentFile.name}")
                    return@forEach
                }

                newReagents[reagent.id] = reagent
            }

        reagents = newReagents
    }
}