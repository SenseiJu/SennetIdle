package me.senseiju.sennetidle.reagents

import dev.triumphteam.gui.builder.item.ItemBuilder
import kotlinx.serialization.Serializable
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserReagent
import me.senseiju.sentils.extensions.primitives.color
import me.senseiju.sentils.serializers.MaterialSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.Style
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private val reagentService = serviceProvider.get<ReagentService>()

@Serializable
data class Reagent(
    val id: String,
    @Serializable(MaterialSerializer::class) val material: Material,
    val displayName: String,
    val craftingTime: Float = -1F,
    val craftingReagents: List<CraftingReagent> = listOf(),
    val upgrades: List<Upgrade> = listOf(),
    val damagePerSecond: Double = 0.0
) {
    fun displayItem(userReagent: UserReagent): ItemStack {
        return ItemBuilder.from(material)
            .name(text(displayName, YELLOW))
            .lore(
                newline(),
                text("Amount: ", AQUA).append(text("${userReagent.amount}", GRAY)),
                text("Total amount crafted: ", AQUA).append(text("${userReagent.totalAmountCrafted}", GRAY)),
                text("Crafting Reagents: ", AQUA).append(text("[${craftingReagents.joinToString(", ") { "x${it.amount} ${it.getDisplayName()}" }}]", GRAY))
            )
            .build()
    }

    fun calculateCraftingTimeInTicks(userReagent: UserReagent): Float {
        val timeToDecrease = 1 - (upgrades.lastOrNull { userReagent.totalAmountCrafted >= it.amountToCraft }?.craftingTimeDecrease ?: 0F)

        return craftingTime * 20 * timeToDecrease
    }
}

@Serializable
data class CraftingReagent(
    val id: String,
    val amount: Long
) {
    fun getDisplayName() = reagentService.reagents[id]!!.displayName
}

@Serializable
data class Upgrade(
    val amountToCraft: Int,
    val craftingTimeDecrease: Float
)