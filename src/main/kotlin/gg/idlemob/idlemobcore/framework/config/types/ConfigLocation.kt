package gg.idlemob.idlemobcore.framework.config.types

import org.bukkit.Bukkit
import org.bukkit.Location
import org.spongepowered.configurate.objectmapping.ConfigSerializable

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
