package me.senseiju.sennetidle.reagents

import dev.triumphteam.gui.builder.item.ItemBuilder
import me.senseiju.sennetidle.reagents.reagentData.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

enum class Reagent(
    val displayName: String,
    private val material: Material,
    private val modelData: Int = 0,
    private val data: BaseReagent
) {
    WOOD("Wooden Plank", Material.OAK_PLANKS, Wood()),
    WOOD_HANDLE("Wooden Handle", Material.WOODEN_SWORD, 100, WoodHandle()),
    WOOD_BLADE("Wooden Blade", Material.WOODEN_SWORD, 200, WoodBlade()),
    WOOD_SWORD("Wooden Sword", Material.WOODEN_SWORD, WoodSword()),

    STONE("Stone", Material.STONE, Stone()),
    STONE_BLADE("Stone Blade", Material.STONE_SWORD, 200, StoneBlade()),
    STONE_SWORD("Stone Sword", Material.STONE_SWORD, StoneSword()),

    IRON("Iron", Material.IRON_INGOT, Iron()),
    IRON_BLADE("Iron Blade", Material.IRON_SWORD, 200, IronBlade()),
    IRON_SWORD("Iron Sword", Material.IRON_SWORD, IronSword()),

    DIAMOND("Diamond", Material.DIAMOND, Diamond()),
    DIAMOND_BLADE("Diamond Blade", Material.DIAMOND_SWORD, 200, DiamondBlade()),
    DIAMOND_SWORD("Diamond Sword", Material.DIAMOND_SWORD, DiamondSword());

    constructor(displayName: String, material: Material, data: BaseReagent) : this(displayName, material, 0, data)

    companion object {
        fun emptyUserMap() = values().associateWithTo(EnumMap(Reagent::class.java)) { 0 }

        val droppable = values().filter { it.isDroppable() }
        val damaging = values().filter { it.isDamaging() }
        val craftable = values().filter { it.isCraftable() }
    }

    fun toItemStack(): ItemStack {
        return ItemBuilder.from(material)
            .name(Component.text(displayName).color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
            .let {
                if (modelData > 0) {
                    it.model(modelData)
                }

                it.build()
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseReagent> dataAs(): T {
        return data as T
    }

    fun isDroppable(): Boolean {
        return data is DroppableReagent
    }

    fun isDamaging(): Boolean {
        return data is DamagingReagent
    }

    fun isCraftable(): Boolean {
        return data is CraftableReagent
    }
}