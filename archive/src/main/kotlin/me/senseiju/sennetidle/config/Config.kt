package me.senseiju.sennetidle.config

import org.bukkit.Bukkit
import org.bukkit.Location
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class Config {
    @Comment("Seconds between saving user data to the database")
    var saveInterval = 120

    var storage = StorageConfig()

    var idleMob = IdleMobConfig()

    @Comment("FancyNpcs npc names that open a menu when clicked")
    var npcs = NpcConfig()
}

@ConfigSerializable
class StorageConfig {
    var host = "localhost"
    var port = 3306
    var database = "sennetidle"
    var username = "root"
    var password = "toor"
    var maximumPoolSize = 10
}

@ConfigSerializable
class IdleMobConfig {
    var spawnLocation = ConfigLocation("world", 100.0, 100.0, 100.0)

    @Comment("Players within this radius of the spawn location are hidden from each other")
    var playerInvisibilityRadius = 5.0

    var hologramLocations = listOf(
        ConfigLocation("world", 100.0, 103.5, 106.0),
        ConfigLocation("world", 106.0, 103.5, 100.0),
        ConfigLocation("world", 94.0, 103.5, 100.0),
        ConfigLocation("world", 100.0, 103.5, 94.0),
    )

    @Comment("MiniMessage formatted, supports PlaceholderAPI placeholders")
    var hologramLines = listOf(
        "<gold><b>Wave %idle_user_current_wave%",
        "",
        "<aqua><b>Health: <yellow>%idle_user_mob_health%",
        "<aqua><b>CPS: <yellow>%idle_user_cps%",
        "<aqua><b>DPS: <yellow>%idle_user_dps%",
    )

    @Comment("Milliseconds between hologram text refreshes")
    var hologramUpdateInterval = 50
}

@ConfigSerializable
class NpcConfig {
    var reagents = listOf("reagents")
    var upgrades = listOf("upgrades")
}

@ConfigSerializable
class ConfigLocation(
    var world: String = "world",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var yaw: Float = 0F,
    var pitch: Float = 0F,
) {
    fun toLocation(): Location {
        val bukkitWorld = Bukkit.getWorld(world) ?: throw IllegalStateException("World '$world' does not exist")
        return Location(bukkitWorld, x, y, z, yaw, pitch)
    }
}
