package gg.idlemob.idlemobcore.framework.config

import gg.idlemob.idlemobcore.framework.config.serializers.DurationSerializer
import gg.idlemob.idlemobcore.framework.config.serializers.NamespacedKeySerializer
import gg.idlemob.idlemobcore.framework.config.serializers.SuffixedDoubleSerializer
import org.bukkit.NamespacedKey
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.util.NamingSchemes
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

private val objectMapperFactory = ObjectMapper.factoryBuilder()
    .defaultNamingScheme(NamingSchemes.LOWER_CASE_DASHED)
    .build()

class ConfigFile<T : Any>(
    private val type: Class<T>,
    val path: Path,
    private val writeDefaults: Boolean = true,
) {
    private val loader = YamlConfigurationLoader.builder()
        .path(path)
        .indent(2)
        .nodeStyle(NodeStyle.BLOCK)
        .defaultOptions { options ->
            options.implicitInitialization(false).serializers {
                it.register(Duration::class.java, DurationSerializer)
                    .register(NamespacedKey::class.java, NamespacedKeySerializer)
                    .register(Double::class.javaObjectType, SuffixedDoubleSerializer)
                    .registerAnnotatedObjects(objectMapperFactory)
            }
        }
        .build()

    lateinit var config: T
        private set

    init {
        reload()
    }

    fun reload() {
        Files.createDirectories(path.parent)

        val root = loader.load()
        val loaded = try {
            root.get(type) ?: run {
                root.set(type, type.getDeclaredConstructor().newInstance())
                if (writeDefaults) {
                    loader.save(root)
                }
                root.get(type)!!
            }
        } catch (ex: ConfigurateException) {
            val reason = ex.cause?.takeIf { it !is ConfigurateException }?.message ?: ex.rawMessage()
            throw IllegalStateException("${path.fileName} ${ex.path()}: $reason", ex)
        }

        if (writeDefaults) {
            root.set(type, loaded)
            loader.save(root)
        }

        config = loaded
    }
}

inline fun <reified T : Any> ConfigFile(path: Path, writeDefaults: Boolean = true) =
    ConfigFile(T::class.java, path, writeDefaults)
