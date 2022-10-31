package me.senseiju.sennetidle.powers

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.powers.powerData.AdrenalineRush
import me.senseiju.sennetidle.powers.powerData.BasePower
import me.senseiju.sennetidle.powers.powerData.Fireball
import org.bukkit.NamespacedKey
import java.util.*

enum class Power(val data: BasePower) {
    FIREBALL(Fireball),
    ADRENALINE_RUSH(AdrenalineRush);

    companion object {
        val namespacedKey = NamespacedKey(plugin, "power")
        fun emptyMap() = values().associateWithTo(EnumMap(Power::class.java)) { 0L }
    }
}