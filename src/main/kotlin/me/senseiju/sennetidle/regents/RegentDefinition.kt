package me.senseiju.sennetidle.regents

import dev.triumphteam.gui.builder.item.ItemBuilder
import kotlinx.serialization.Serializable
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserRegent
import me.senseiju.sentils.serializers.MaterialSerializer
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private val regentService = serviceProvider.get<RegentService>()

@Serializable
data class Regent(
    val id: String,
    @Serializable(MaterialSerializer::class) val material: Material,
    val displayName: String,
    val space: Float,
    val value: Double,
    val craftingTime: Float = -1F,
    val craftingRegents: List<CraftingRegent>,
    val upgrades: List<Upgrade>
) {
    fun displayItem(userRegent: UserRegent): ItemStack {
        return ItemBuilder.from(material)
            .name(text(displayName, YELLOW))
            .lore(
                newline(),
                text("Level: ", AQUA).append(text("${userRegent.level}", GRAY)),
                text("Amount: ", AQUA).append(text("${userRegent.amount}", GRAY)),
                text("Crafting Regents: ", AQUA).append(text("[${craftingRegents.joinToString(", ") { "x${it.amount} ${it.getDisplayName()}" }}]", GRAY))
            )
            .build()
    }
}

@Serializable
data class CraftingRegent(
    val id: String,
    val amount: Int
) {
    fun getDisplayName() = regentService.regents[id]!!.displayName
}

@Serializable
data class Upgrade(
    val amountToCraft: Int,
    val craftingTimeDecrease: Float
)