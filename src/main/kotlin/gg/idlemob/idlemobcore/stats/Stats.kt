package gg.idlemob.idlemobcore.stats

import gg.idlemob.idlemobcore.user.User
import org.bukkit.entity.Player
import java.util.*

enum class Stat {
    DPS,
    CLICK_DAMAGE_PERCENT,
    CPS_LIMIT,
    DROP_CHANCE,
    DROP_AMOUNT,
    BOSS_TIME_LIMIT,
    CRIT_CHANCE,
    INSTANT_KILL_CHANCE,
    BOX_DROP_CHANCE,
}

class StatContext(val player: Player, val user: User, val boss: Boolean)

class StatModifiers {
    private val flat = EnumMap<Stat, Double>(Stat::class.java)
    private val percent = EnumMap<Stat, Double>(Stat::class.java)
    private val multiplier = EnumMap<Stat, Double>(Stat::class.java)

    fun add(stat: Stat, amount: Double) {
        flat.merge(stat, amount, Double::plus)
    }

    fun addPercent(stat: Stat, amount: Double) {
        percent.merge(stat, amount, Double::plus)
    }

    fun multiply(stat: Stat, factor: Double) {
        multiplier.merge(stat, factor, Double::times)
    }

    internal fun apply(stat: Stat, base: Double): Double {
        val withFlat = base + (flat[stat] ?: 0.0)
        val withPercent = withFlat * (1 + (percent[stat] ?: 0.0) / 100)
        return (withPercent * (multiplier[stat] ?: 1.0)).coerceAtLeast(0.0)
    }
}

fun interface StatProvider {
    fun modify(context: StatContext, modifiers: StatModifiers)
}

class Stats(private val values: Map<Stat, Double>) {
    operator fun get(stat: Stat): Double = values[stat] ?: 0.0
}

class StatService {
    private val providers = mutableListOf<StatProvider>()

    fun register(provider: StatProvider) {
        providers += provider
    }

    fun calculate(context: StatContext, bases: Map<Stat, Double>): Stats {
        val modifiers = StatModifiers()
        providers.forEach { it.modify(context, modifiers) }

        return Stats(Stat.entries.associateWith { modifiers.apply(it, bases[it] ?: 0.0) })
    }
}
