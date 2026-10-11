package gg.idlemob.idlemobcore.resource

import gg.idlemob.idlemobcore.framework.config.types.ItemDisplay
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.PostProcess
import java.time.Duration

@ConfigSerializable
class ResourceConfig {
    var display = ItemDisplay()
    var sortOrder = 0
    var unlock = ResourceUnlock()
    var components = ResourceComponents()
}

@ConfigSerializable
class ResourceUnlock {
    var stage = 1
    var prestige = 0

    @PostProcess
    private fun validate() {
        require(stage >= 1) { "stage must be at least 1" }
        require(prestige >= 0) { "prestige can't be negative" }
    }
}

@ConfigSerializable
class ResourceComponents {
    var damaging: DamagingComponent? = null
    var droppable: DroppableComponent? = null
    var craftable: CraftableComponent? = null
    var tradeable: TradeableComponent? = null
    var blueprint: BlueprintComponent? = null
}

@ConfigSerializable
class DamagingComponent {
    var dps = 0.0
    var target = DamageTarget.ALL

    @PostProcess
    private fun validate() {
        require(dps >= 0) { "dps can't be negative" }
    }
}

enum class DamageTarget { ALL, MOBS, BOSSES }

@ConfigSerializable
class DroppableComponent {
    var mob: Drop? = null
    var boss: Drop? = null
}

@ConfigSerializable
class Drop {
    var chance = 100.0
    var amount = DropAmount()
    var stageBonus = 0.0

    @PostProcess
    private fun validate() {
        require(chance in 0.0..100.0) { "chance must be between 0 and 100" }
        require(stageBonus >= 0) { "stage-bonus can't be negative" }
    }
}

@ConfigSerializable
class DropAmount {
    var min = 1
    var max = 1

    @PostProcess
    private fun validate() {
        require(min >= 0) { "min can't be negative" }
        require(min <= max) { "min can't be bigger than max" }
    }
}

@ConfigSerializable
class CraftableComponent {
    var ingredients: Map<String, Int> = emptyMap()
    var output = 1

    @PostProcess
    private fun validate() {
        require(ingredients.isNotEmpty()) { "ingredients must have at least one resource" }
        require(ingredients.values.all { it >= 1 }) { "ingredient amounts must be at least 1" }
        require(output >= 1) { "output must be at least 1" }
    }
}

@ConfigSerializable
class TradeableComponent {
    var enabled = true
}

@ConfigSerializable
class BlueprintComponent {
    var craftTime: Duration = Duration.ofMinutes(1)
    var amount = 1

    @PostProcess
    private fun validate() {
        require(!craftTime.isNegative && !craftTime.isZero) { "craft-time must be longer than 0" }
        require(amount >= 1) { "amount must be at least 1" }
    }
}
