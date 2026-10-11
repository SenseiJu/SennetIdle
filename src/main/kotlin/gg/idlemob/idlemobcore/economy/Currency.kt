package gg.idlemob.idlemobcore.economy

import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import java.math.BigDecimal
import java.math.RoundingMode

enum class Currency(
    val id: String,
    val displayName: String,
    val color: String,
    val symbol: String,
    val startingBalance: Double,
    val decimals: Int,
    val payable: Boolean,
) {
    MONEY("money", "Money", "<green>", "$", 0.0, 2, true),
    GEMS("gems", "Gems", "<aqua>", "◆", 0.0, 0, false);

    fun format(amount: Double): String = "$symbol${amount.formatSuffixed()}"

    fun formatColored(amount: Double): String = "$color${format(amount)}"

    fun round(amount: Double): Double = BigDecimal.valueOf(amount).setScale(decimals, RoundingMode.DOWN).toDouble()

    companion object {
        fun byId(id: String): Currency? = entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
