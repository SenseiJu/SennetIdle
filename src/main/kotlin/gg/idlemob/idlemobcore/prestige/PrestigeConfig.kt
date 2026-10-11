package gg.idlemob.idlemobcore.prestige

import gg.idlemob.idlemobcore.economy.Currency
import gg.idlemob.idlemobcore.framework.config.types.ItemDisplay
import gg.idlemob.idlemobcore.stats.Stat
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.PostProcess

@ConfigSerializable
class PrestigeLevelConfig {
    var requiredStage = 1
    var resources: Map<String, Double> = emptyMap()
    var cost: Map<String, Double> = emptyMap()
    var points = 1

    fun costs(): Map<Currency, Double> = cost.mapNotNull { (id, amount) -> Currency.byId(id)?.let { it to amount } }.toMap()

    @PostProcess
    private fun validate() {
        require(requiredStage >= 1) { "required-stage must be at least 1" }
        require(resources.values.all { it > 0 }) { "resource amounts must be above 0" }
        require(cost.keys.all { Currency.byId(it) != null }) { "unknown currency in ${cost.keys}, use one of ${Currency.entries.map { it.id }}" }
        require(cost.values.all { it > 0 }) { "cost amounts must be above 0" }
        require(points >= 0) { "points can't be negative" }
    }
}

enum class UpgradeMode { FLAT, PERCENT }

@ConfigSerializable
class UpgradeConfig {
    var display = ItemDisplay()
    var sortOrder = 0
    var stat = Stat.CLICK_DAMAGE_PERCENT
    var mode = UpgradeMode.FLAT
    var perLevel = 1.0
    var maxLevel = 10
    var cost = UpgradeCost()

    fun costFor(currentLevel: Int): Int = cost.base + cost.perLevel * currentLevel

    @PostProcess
    private fun validate() {
        require(perLevel > 0) { "per-level must be above 0" }
        require(maxLevel >= 1) { "max-level must be at least 1" }
    }
}

@ConfigSerializable
class UpgradeCost {
    var base = 1
    var perLevel = 0

    @PostProcess
    private fun validate() {
        require(base >= 1) { "base must be at least 1" }
        require(perLevel >= 0) { "per-level can't be negative" }
    }
}
