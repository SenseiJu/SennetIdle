package gg.idlemob.idlemobcore.box

import gg.idlemob.idlemobcore.framework.config.types.ItemDisplay
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.PostProcess

@ConfigSerializable
class BoxConfig {
    var display = ItemDisplay().apply {
        material = Material.CHEST
        itemModel = NamespacedKey.minecraft("chest")
    }
    var merge: BoxMerge? = null
    var guaranteedRewards: List<BoxReward> = emptyList()
    var randomRewards: List<RandomBoxReward> = emptyList()

    @PostProcess
    private fun validate() {
        require(guaranteedRewards.isNotEmpty() || randomRewards.isNotEmpty()) { "a box needs at least one guaranteed or random reward" }
    }
}

@ConfigSerializable
class BoxMerge {
    var into = ""
    var amount = 4

    @PostProcess
    private fun validate() {
        require(into.isNotBlank()) { "into must be set" }
        require(amount >= 2) { "amount must be at least 2" }
    }
}

@ConfigSerializable
class BoxReward {
    var name = ""
    var commands: List<String> = emptyList()

    @PostProcess
    private fun validate() {
        require(name.isNotBlank()) { "every reward needs a name" }
    }
}

@ConfigSerializable
class RandomBoxReward {
    var name = ""
    var weight = 1.0
    var commands: List<String> = emptyList()

    @PostProcess
    private fun validate() {
        require(name.isNotBlank()) { "every reward needs a name" }
        require(weight > 0) { "weight must be above 0" }
    }
}
