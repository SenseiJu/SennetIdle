package gg.idlemob.idlemobcore.framework.config.serializers

import gg.idlemob.idlemobcore.framework.extensions.parseSuffixed
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

object SuffixedDoubleSerializer : ScalarSerializer<Double>(Double::class.javaObjectType) {

    override fun deserialize(type: Type, obj: Any): Double {
        if (obj is Number) {
            return obj.toDouble()
        }

        return obj.toString().parseSuffixed() ?: throw SerializationException("invalid number '$obj', use e.g. 2500 or 1.2M")
    }

    override fun serialize(item: Double, typeSupported: Predicate<Class<*>>): Any {
        return item
    }
}
