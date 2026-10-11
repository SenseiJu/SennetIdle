package gg.idlemob.idlemobcore

import gg.idlemob.idlemobcore.framework.config.types.ConfigLocation
import gg.idlemob.idlemobcore.framework.database.DatabaseConfig
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.PostProcess
import java.time.Duration
import kotlin.math.pow

@ConfigSerializable
class Config {
    var database = DatabaseConfig()
    var autoSaveInterval: Duration = Duration.ofSeconds(30)
    var fightZone = FightZoneConfig()
    var playerAttack = PlayerAttackConfig()
    var stages = StagesConfig()
    var generators = GeneratorsConfig()

    @PostProcess
    private fun validate() {
        require(autoSaveInterval >= Duration.ofSeconds(5)) { "auto-save-interval must be at least 5s" }
    }
}

@ConfigSerializable
class FightZoneConfig {
    var mobLocation = ConfigLocation("idle", 0.5, 64.0, 12.5, 180F)

    @PostProcess
    private fun validate() {
        require(Bukkit.getWorld(mobLocation.world) != null) { "mob-location world '${mobLocation.world}' doesn't exist" }
    }
}

@ConfigSerializable
class PlayerAttackConfig {
    var baseDps = 10.0
    var baseCpsLimit = 10
    var clickDamagePercent = 15.0

    @PostProcess
    private fun validate() {
        require(baseDps >= 0) { "base-dps can't be negative" }
        require(baseCpsLimit >= 1) { "base-cps-limit must be at least 1" }
        require(clickDamagePercent >= 0) { "click-damage-percent can't be negative" }
    }
}

@ConfigSerializable
class StagesConfig {
    var wavesPerStage = 15
    var baseBossTimeLimit: Duration = Duration.ofSeconds(30)
    var baseMobHealth = 100.0
    var healthGrowth = 1.5
    var bossHealthMultiplier = 8.0
    var mobTypes = listOf(
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER, EntityType.BLAZE,
        EntityType.ENDERMAN, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.ZOMBIFIED_PIGLIN,
    )
    var bossTypes = listOf(EntityType.IRON_GOLEM, EntityType.WARDEN)

    fun mobHealth(stage: Int, boss: Boolean): Double {
        val health = baseMobHealth * healthGrowth.pow(stage - 1)
        return if (boss) health * bossHealthMultiplier else health
    }

    @PostProcess
    private fun validate() {
        require(wavesPerStage >= 1) { "waves-per-stage must be at least 1" }
        require(!baseBossTimeLimit.isNegative && !baseBossTimeLimit.isZero) { "base-boss-time-limit must be longer than 0" }
        require(baseMobHealth > 0) { "base-mob-health must be above 0" }
        require(healthGrowth >= 1) { "health-growth must be at least 1" }
        require(bossHealthMultiplier > 0) { "boss-health-multiplier must be above 0" }
        require(mobTypes.isNotEmpty() && mobTypes.all { it.isAlive }) { "mob-types must be a list of living mobs" }
        require(bossTypes.isNotEmpty() && bossTypes.all { it.isAlive }) { "boss-types must be a list of living mobs" }
    }
}

@ConfigSerializable
class GeneratorsConfig {
    var slots: List<GeneratorSlotConfig> = emptyList()
}

@ConfigSerializable
class GeneratorSlotConfig {
    var location = ConfigLocation()
    var unlockStage = 1
    var unlockPrestige = 0
    var permission = ""

    @PostProcess
    private fun validate() {
        require(Bukkit.getWorld(location.world) != null) { "location world '${location.world}' doesn't exist" }
        require(unlockStage >= 1) { "unlock-stage must be at least 1" }
        require(unlockPrestige >= 0) { "unlock-prestige can't be negative" }
    }
}
