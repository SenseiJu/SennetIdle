package gg.idlemob.idlemobcore.user

import gg.idlemob.idlemobcore.economy.Currency
import gg.idlemob.idlemobcore.generator.GeneratorState
import java.util.*

class User(
    val uuid: UUID,
    val createdAt: Long,
    stage: Int = 1,
    wave: Int = 1,
    prestige: Int = 0,
    prestigePoints: Int = 0,
    offlineTimeMs: Long = 0,
    resources: Map<String, Double> = emptyMap(),
    balances: Map<Currency, Double> = emptyMap(),
    prestigeProgress: Map<String, Double> = emptyMap(),
    upgrades: Map<String, Int> = emptyMap(),
    generators: Collection<GeneratorState> = emptyList(),
) {
    private val resources = HashMap(resources)
    private val balances = EnumMap<Currency, Double>(Currency::class.java).apply { putAll(balances) }
    private val prestigeProgress = HashMap(prestigeProgress)
    private val upgrades = HashMap(upgrades)
    private val generators = TreeMap(generators.associateBy { it.slot })

    var stage = stage
        @Synchronized get
        @Synchronized set
    var wave = wave
        @Synchronized get
        @Synchronized set
    var prestige = prestige
        @Synchronized get
        @Synchronized set
    var prestigePoints = prestigePoints
        @Synchronized get
        @Synchronized set
    var offlineTimeMs = offlineTimeMs
        @Synchronized get
        @Synchronized set

    @Synchronized
    fun getResource(resource: String): Double = resources[resource] ?: 0.0

    @Synchronized
    fun setResource(resource: String, amount: Double) {
        resources[resource] = amount.coerceAtLeast(0.0)
    }

    @Synchronized
    fun addResource(resource: String, amount: Double) {
        setResource(resource, getResource(resource) + amount)
    }

    @Synchronized
    fun resources(): Map<String, Double> = HashMap(resources)

    @Synchronized
    fun getBalance(currency: Currency): Double = balances[currency] ?: 0.0

    @Synchronized
    internal fun cacheBalance(currency: Currency, amount: Double) {
        balances[currency] = amount
    }

    @Synchronized
    fun getPrestigeProgress(resource: String): Double = prestigeProgress[resource] ?: 0.0

    @Synchronized
    fun addPrestigeProgress(resource: String, amount: Double) {
        prestigeProgress[resource] = getPrestigeProgress(resource) + amount
    }

    @Synchronized
    fun clearPrestigeProgress() {
        prestigeProgress.clear()
    }

    @Synchronized
    fun getUpgrade(upgrade: String): Int = upgrades[upgrade] ?: 0

    @Synchronized
    fun setUpgrade(upgrade: String, level: Int) {
        upgrades[upgrade] = level
    }

    @Synchronized
    fun upgrades(): Map<String, Int> = HashMap(upgrades)

    @Synchronized
    fun getGenerator(slot: Int): GeneratorState? = generators[slot]

    @Synchronized
    fun setGenerator(state: GeneratorState) {
        generators[state.slot] = state
    }

    @Synchronized
    fun removeGenerator(slot: Int): GeneratorState? = generators.remove(slot)

    @Synchronized
    fun generators(): List<GeneratorState> = generators.values.toList()

    @Synchronized
    internal fun snapshot() = UserSnapshot(
        uuid = uuid,
        stage = stage,
        wave = wave,
        prestige = prestige,
        prestigePoints = prestigePoints,
        offlineTimeMs = offlineTimeMs,
        resources = HashMap(resources),
        prestigeProgress = HashMap(prestigeProgress),
        upgrades = HashMap(upgrades),
        generators = generators.values.map { it.copy() },
    )
}

data class UserSnapshot(
    val uuid: UUID,
    val stage: Int,
    val wave: Int,
    val prestige: Int,
    val prestigePoints: Int,
    val offlineTimeMs: Long,
    val resources: Map<String, Double>,
    val prestigeProgress: Map<String, Double>,
    val upgrades: Map<String, Int>,
    val generators: List<GeneratorState>,
)
