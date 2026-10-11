package gg.idlemob.idlemobcore.blueprint

import gg.idlemob.idlemobcore.resource.BlueprintComponent
import gg.idlemob.idlemobcore.resource.ResourceConfig
import gg.idlemob.idlemobcore.resource.ResourceRegistry
import java.time.Duration

class Blueprint(val id: String, val resource: ResourceConfig, private val component: BlueprintComponent) {
    val craftTime: Duration get() = component.craftTime
    val amount: Int get() = component.amount
    val outputName: String get() = resource.display.name
    val name: String get() = "$outputName <gray>Blueprint"
}

class Blueprints(private val resources: ResourceRegistry) {

    val ids: Set<String>
        get() = resources.entries.filterValues { it.components.blueprint != null }.keys

    operator fun get(id: String): Blueprint? {
        val resource = resources[id] ?: return null
        val component = resource.components.blueprint ?: return null
        return Blueprint(id, resource, component)
    }

    operator fun contains(id: String): Boolean = get(id) != null
}
