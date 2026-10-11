package gg.idlemob.idlemobcore.box

import gg.idlemob.idlemobcore.framework.content.ContentProblem
import gg.idlemob.idlemobcore.framework.content.ContentRegistry
import gg.idlemob.idlemobcore.framework.content.log
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import java.nio.file.Path

class BoxRegistry(folder: Path) {
    private val registry = ContentRegistry<BoxConfig>(folder)

    val entries: Map<String, BoxConfig>
        get() = registry.entries

    operator fun get(id: String): BoxConfig? = registry[id]

    operator fun contains(id: String): Boolean = id in registry

    fun reload(): List<ContentProblem> {
        val load = registry.load()
        val problems = load.problems.toMutableList()
        val valid = LinkedHashMap(load.entries)

        removeMissingMerges(valid, load.files, problems)
        removeMergeLoops(valid, load.files, problems)
        removeMissingMerges(valid, load.files, problems)

        registry.apply(valid)

        problems.log()
        EventLogger.info("boxes_loaded") {
            "count" to valid.size
            "problems" to problems.size
        }

        return problems
    }

    private fun removeMissingMerges(
        boxes: MutableMap<String, BoxConfig>,
        files: Map<String, String>,
        problems: MutableList<ContentProblem>,
    ) {
        while (true) {
            val (id, box) = boxes.entries.firstOrNull { (_, box) -> box.merge?.let { it.into !in boxes } == true } ?: return

            problems += ContentProblem(files.getValue(id), "merges into missing or broken box '${box.merge!!.into}'")
            boxes.remove(id)
        }
    }

    private fun removeMergeLoops(
        boxes: MutableMap<String, BoxConfig>,
        files: Map<String, String>,
        problems: MutableList<ContentProblem>,
    ) {
        val looping = LinkedHashSet<String>()

        boxes.keys.forEach { start ->
            val path = LinkedHashSet<String>()
            var current: String? = start
            while (current != null && path.add(current)) {
                current = boxes[current]?.merge?.into
            }
            if (current != null) {
                looping += path.dropWhile { it != current }
            }
        }

        looping.forEach { id ->
            problems += ContentProblem(files.getValue(id), "merge chain loops back to itself through ${looping.joinToString(" -> ")}")
            boxes.remove(id)
        }
    }
}
