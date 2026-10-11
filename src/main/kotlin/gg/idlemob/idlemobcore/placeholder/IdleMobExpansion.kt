package gg.idlemob.idlemobcore.placeholder

import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.economy.Currency
import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import gg.idlemob.idlemobcore.stats.Stat
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class IdleMobExpansion(private val plugin: IdleMobCore) : PlaceholderExpansion() {

    override fun getIdentifier() = "idlemob"

    override fun getAuthor() = plugin.pluginMeta.authors.joinToString()

    override fun getVersion() = plugin.pluginMeta.version

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val user = player?.let { plugin.users[it.uniqueId] } ?: return ""
        val session = player.player?.let { plugin.fight.find(it) }

        if (params.startsWith("balance_")) {
            val raw = params.endsWith("_raw")
            val currency = Currency.byId(params.removePrefix("balance_").removeSuffix("_raw")) ?: return null
            val amount = user.getBalance(currency)
            return if (raw) amount.toString() else currency.format(amount)
        }

        if (params.startsWith("resource_")) {
            return user.getResource(params.removePrefix("resource_")).formatSuffixed()
        }

        return when (params) {
            "stage" -> user.stage.toString()
            "wave" -> user.wave.toString()
            "waves_per_stage" -> plugin.cfg.stages.wavesPerStage.toString()
            "prestige" -> user.prestige.toString()
            "prestige_points" -> user.prestigePoints.toString()
            "dps" -> session?.stats?.get(Stat.DPS)?.formatSuffixed() ?: "0"
            "click_damage" -> session?.stats?.let { (it[Stat.DPS] * it[Stat.CLICK_DAMAGE_PERCENT] / 100).formatSuffixed() } ?: "0"
            "cps" -> session?.cps?.toString() ?: "0"
            "cps_limit" -> session?.stats?.get(Stat.CPS_LIMIT)?.toInt()?.toString() ?: "0"
            "mob_health" -> session?.health?.coerceAtLeast(0.0)?.formatSuffixed() ?: "0"
            "mob_max_health" -> session?.maxHealth?.formatSuffixed() ?: "0"
            "boss" -> (session?.boss == true).toString()
            "boss_time_left" -> session?.takeIf { it.boss }?.bossSecondsLeft?.toString() ?: "0"
            else -> null
        }
    }
}
