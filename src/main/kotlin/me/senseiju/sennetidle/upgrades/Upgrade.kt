package me.senseiju.sennetidle.upgrades

import me.senseiju.sennetidle.upgrades.upgradeData.*
import java.util.*

enum class Upgrade(val data: BaseUpgrade) {
    SHARPNESS(Sharpness),
    CRITICAL(Critical),
    HARVESTER(Harvester),

    EXPLOSIVE(Explosive);

    companion object {
        fun emptyMap() = values().associateWithTo(EnumMap(Upgrade::class.java)) { 0 }

        /*
        The upgrades are in an order of execution for idle mob processing
         */
        val playerUpgrades = setOf(SHARPNESS, CRITICAL, HARVESTER)

        /*
        The upgrades are in an order of execution for idle mob processing
         */
        val idleMobUpgrades = setOf(EXPLOSIVE)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseUpgrade> dataAs(): T {
        return data as T
    }
}