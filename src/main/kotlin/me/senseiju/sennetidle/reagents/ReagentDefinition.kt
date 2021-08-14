package me.senseiju.sennetidle.reagents

import dev.triumphteam.gui.builder.item.ItemBuilder
import kotlinx.serialization.Serializable
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserReagent
import me.senseiju.sentils.serializers.MaterialSerializer
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private val reagentService = serviceProvider.get<ReagentService>()

@Serializable
data class Reagent(
    val id: String,
    @Serializable(MaterialSerializer::class) val material: Material,
    val displayName: String,
    val space: Float,
    val value: Double,
    val craftingTime: Float = -1F,
    val craftingReagents: List<CraftingReagent>,
    val upgrades: List<Upgrade>
) {
    fun displayItem(userReagent: UserReagent): ItemStack {
        return ItemBuilder.from(material)
            .name(text(displayName, YELLOW))
            .lore(
                newline(),
                text("Level: ", AQUA).append(text("${userReagent.totalAmountCrafted}", GRAY)),
                text("Amount: ", AQUA).append(text("${userReagent.amount}", GRAY)),
                text("Crafting Reagents: ", AQUA).append(text("[${craftingReagents.joinToString(", ") { "x${it.amount} ${it.getDisplayName()}" }}]", GRAY))
            )
            .build()
    }

    fun calculateCraftingTime(userReagent: UserReagent): Float {
        val timeToDecrease = 1 - (upgrades.firstOrNull { it.amountToCraft >= userReagent.totalAmountCrafted }?.craftingTimeDecrease ?: 0F)

        return craftingTime * timeToDecrease
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