package me.senseiju.sennetidle.regents

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sentils.service.Service
import java.io.File
import java.util.jar.JarFile

class RegentService(private val plugin: SennetIdle) : Service() {
    private val logger = plugin.slF4JLogger

    var regents = hashMapOf<String, Regent>()
        private set

    init {
        copyRegentsFromJar()
        loadRegents()
    }

    override fun onReload() {
        loadRegents()
    }

    private fun copyRegentsFromJar() {
        val jar = JarFile(plugin.javaClass.protectionDomain.codeSource.location.toURI().path)
        val entries = jar.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()

            if (!entry.name.startsWith("regents/")) {
                continue
            }

            if (!File(plugin.dataFolder, entry.name).exists()) {
                plugin.saveResource(entry.name, false)
            }
        }
    }

    private fun loadRegents() {
        val newRegents = HashMap<String, Regent>()

        File(plugin.dataFolder, "regents/")
            .walk()
            .filter { it.isFile }
            .forEach { regentFile ->
                val regentJson = regentFile.readText()

                val regent = try {
                    Json.decodeFromString<Regent>(regentJson)
                } catch (ex: Exception) {
                    logger.error("Regent failed to decode. File: ${regentFile.name}")
                    ex.printStackTrace()
                    return@forEach
                }

                if (newRegents.containsKey(regent.id)) {
                    logger.error("Regent failed to load as it already exists. RegentId: ${regent.id}, File: ${regentFile.name}")
                    return@forEach
                }

                newRegents[regent.id] = regent
            }

        regents = newRegents
    }
}