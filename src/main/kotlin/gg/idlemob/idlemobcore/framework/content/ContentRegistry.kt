package gg.idlemob.idlemobcore.framework.content

import gg.idlemob.idlemobcore.framework.config.ConfigFile
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension

private val ID_PATTERN = Regex("[a-z0-9_]+")

class ContentRegistry<T : Any>(private val type: Class<T>, private val folder: Path) {
    var entries: Map<String, T> = emptyMap()
        private set

    operator fun get(id: String): T? = entries[id]

    operator fun contains(id: String): Boolean = id in entries

    fun load(): ContentLoad<T> {
        Files.createDirectories(folder)

        val loaded = LinkedHashMap<String, T>()
        val files = HashMap<String, String>()
        val problems = ArrayList<ContentProblem>()

        val paths = Files.list(folder).use { stream ->
            stream.filter { it.isRegularFile() && it.extension == "yml" }.sorted().toList()
        }

        for (path in paths) {
            val file = folder.parent.relativize(path).invariantSeparatorsPathString
            val id = path.nameWithoutExtension

            if (!ID_PATTERN.matches(id)) {
                problems += ContentProblem(file, "file name must be lowercase letters, digits and underscores")
                continue
            }

            try {
                loaded[id] = ConfigFile(type, path, writeDefaults = false).config
                files[id] = file
            } catch (ex: Exception) {
                problems += ContentProblem(file, (ex.message ?: ex.javaClass.simpleName).removePrefix("${path.fileName} "))
            }
        }

        return ContentLoad(loaded, files, problems)
    }

    fun apply(entries: Map<String, T>) {
        this.entries = entries
    }
}

class ContentLoad<T : Any>(
    val entries: Map<String, T>,
    val files: Map<String, String>,
    val problems: List<ContentProblem>,
)

class ContentProblem(val file: String, val message: String)

fun List<ContentProblem>.log() = forEach {
    EventLogger.warn("content_problem") {
        "file" to it.file
        "problem" to it.message
    }
}

inline fun <reified T : Any> ContentRegistry(folder: Path) = ContentRegistry(T::class.java, folder)
