package gg.idlemob.idlemobcore.resource

import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.framework.content.ContentProblem
import gg.idlemob.idlemobcore.framework.content.ContentRegistry
import gg.idlemob.idlemobcore.framework.content.log
import java.nio.file.Path

class ResourceRegistry(folder: Path) {
    private val registry = ContentRegistry<ResourceConfig>(folder)

    val entries: Map<String, ResourceConfig>
        get() = registry.entries

    operator fun get(id: String): ResourceConfig? = registry[id]

    operator fun contains(id: String): Boolean = id in registry

    fun reload(): List<ContentProblem> {
        val load = registry.load()
        val problems = load.problems.toMutableList()
        val valid = LinkedHashMap(load.entries)

        removeMissingIngredients(valid, load.files, problems)
        removeRecipeLoops(valid, load.files, problems)
        removeMissingIngredients(valid, load.files, problems)

        registry.apply(valid)

        problems.log()
        EventLogger.info("resources_loaded") {
            "count" to valid.size
            "problems" to problems.size
        }

        return problems
    }

    private fun removeMissingIngredients(
        resources: MutableMap<String, ResourceConfig>,
        files: Map<String, String>,
        problems: MutableList<ContentProblem>,
    ) {
        while (true) {
            val (id, resource) = resources.entries.firstOrNull { (_, resource) ->
                resource.components.craftable?.ingredients?.keys?.any { it !in resources } == true
            } ?: return

            val missing = resource.components.craftable!!.ingredients.keys.filter { it !in resources }
            problems += ContentProblem(files.getValue(id), "missing or broken ingredient(s): ${missing.joinToString()}")
            resources.remove(id)
        }
    }

    private fun removeRecipeLoops(
        resources: MutableMap<String, ResourceConfig>,
        files: Map<String, String>,
        problems: MutableList<ContentProblem>,
    ) {
        val done = HashSet<String>()
        val path = LinkedHashSet<String>()
        val looping = LinkedHashSet<String>()

        fun visit(id: String) {
            if (id in done) return
            if (!path.add(id)) {
                looping += path.dropWhile { it != id }
                return
            }

            resources[id]?.components?.craftable?.ingredients?.keys?.forEach(::visit)

            path.remove(id)
            done += id
        }

        resources.keys.toList().forEach(::visit)

        looping.forEach { id ->
            problems += ContentProblem(files.getValue(id), "recipe loops back to itself through ${looping.joinToString(" -> ")}")
            resources.remove(id)
        }
    }
}
