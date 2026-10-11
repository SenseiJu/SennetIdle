package gg.idlemob.idlemobcore.prestige

import gg.idlemob.idlemobcore.framework.content.ContentProblem
import gg.idlemob.idlemobcore.framework.content.ContentRegistry
import gg.idlemob.idlemobcore.framework.content.log
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.resource.ResourceRegistry
import java.nio.file.Path

class PrestigeRegistry(folder: Path, private val resources: ResourceRegistry) {
    private val registry = ContentRegistry<PrestigeLevelConfig>(folder)

    var levels: List<PrestigeLevelConfig> = emptyList()
        private set

    fun level(prestige: Int): PrestigeLevelConfig? = levels.getOrNull(prestige - 1)

    fun reload(): List<ContentProblem> {
        val load = registry.load()
        val problems = load.problems.toMutableList()

        val numbered = sortedMapOf<Int, PrestigeLevelConfig>()
        load.entries.forEach { (id, level) ->
            val file = load.files.getValue(id)
            val number = id.toIntOrNull()
            val missing = level.resources.keys.filter { it !in resources }

            when {
                number == null || number < 1 -> problems += ContentProblem(file, "file name must be the prestige level, like 1.yml")
                missing.isNotEmpty() -> problems += ContentProblem(file, "uses missing resource(s): ${missing.joinToString()}")
                else -> numbered[number] = level
            }
        }

        val valid = mutableListOf<PrestigeLevelConfig>()
        for ((number, level) in numbered) {
            if (number != valid.size + 1) {
                problems += ContentProblem(load.files.getValue(number.toString()), "prestige ${valid.size + 1} is missing or broken, so this and later levels are skipped")
                break
            }
            valid += level
        }

        registry.apply(load.entries)
        levels = valid

        problems.log()
        EventLogger.info("prestiges_loaded") {
            "count" to valid.size
            "problems" to problems.size
        }

        return problems
    }
}

class UpgradeRegistry(folder: Path) {
    private val registry = ContentRegistry<UpgradeConfig>(folder)

    val entries: Map<String, UpgradeConfig>
        get() = registry.entries

    operator fun get(id: String): UpgradeConfig? = registry[id]

    fun reload(): List<ContentProblem> {
        val load = registry.load()
        registry.apply(load.entries)

        load.problems.log()
        EventLogger.info("upgrades_loaded") {
            "count" to load.entries.size
            "problems" to load.problems.size
        }

        return load.problems
    }
}
