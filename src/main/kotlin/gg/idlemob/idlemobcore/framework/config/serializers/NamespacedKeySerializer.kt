package gg.idlemob.idlemobcore.framework.config.serializers

import org.bukkit.NamespacedKey
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

object NamespacedKeySerializer : ScalarSerializer<NamespacedKey>(NamespacedKey::class.java) {

    override fun deserialize(type: Type, obj: Any): NamespacedKey {
        return NamespacedKey.fromString(obj.toString().trim())
            ?: throw SerializationException("'$obj' isn't a valid key, use e.g. minecraft:oak_planks or idlemob:iron_blade")
    }

    override fun serialize(item: NamespacedKey, typeSupported: Predicate<Class<*>>): Any {
        return item.asString()
    }
}
