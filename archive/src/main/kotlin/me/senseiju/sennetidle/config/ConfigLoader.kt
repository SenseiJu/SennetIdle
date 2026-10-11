package me.senseiju.sennetidle.config

import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.util.NamingSchemes
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension

private val OBJECT_MAPPER_FACTORY = ObjectMapper.factoryBuilder()
    .defaultNamingScheme(NamingSchemes.LOWER_CASE_DASHED)
    .build()

private fun applyOptions(options: ConfigurationOptions): ConfigurationOptions {
    return options.serializers { it.registerAnnotatedObjects(OBJECT_MAPPER_FACTORY) }
}

/**
 * Loads a [org.spongepowered.configurate.objectmapping.ConfigSerializable] class from `<name>.json` if it exists,
 * otherwise from `<name>.yml`. Missing options are written back to the file with their defaults.
 */
class ConfigLoader<T : Any>(private val type: Class<T>, dataFolder: Path, name: String) {
    val path: Path = sequenceOf("json", "yml", "yaml")
        .map { dataFolder.resolve("$name.$it") }
        .firstOrNull { it.exists() }
        ?: dataFolder.resolve("$name.yml")

    private val loader: AbstractConfigurationLoader<*> = when (path.extension) {
        "json" -> GsonConfigurationLoader.builder()
            .path(path)
            .indent(2)
            .defaultOptions(::applyOptions)
            .build()

        else -> YamlConfigurationLoader.builder()
            .path(path)
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
            .defaultOptions(::applyOptions)
            .build()
    }

    lateinit var config: T
        private set

    init {
        reload()
    }

    fun reload() {
        Files.createDirectories(path.parent)

        val root = loader.load()
        val loaded = root.get(type) ?: type.getDeclaredConstructor().newInstance()

        root.set(type, loaded)
        loader.save(root)

        config = loaded
    }

    fun save() {
        val root = loader.createNode()
        root.set(type, config)
        loader.save(root)
    }
}

inline fun <reified T : Any> ConfigLoader(dataFolder: Path, name: String) = ConfigLoader(T::class.java, dataFolder, name)
