package me.senseiju.sennetidle.reagents

import me.senseiju.sennetidle.reagents.reagentData.*
import java.util.*

enum class Reagent(val data: BaseReagent) {
    WOOD(Wood),
    WOODEN_HANDLE(WoodenHandle),
    WOODEN_BLADE(WoodenBlade),
    WOODEN_SWORD(WoodenSword),

    STONE(Stone),
    STONE_BLADE(StoneBlade),
    STONE_SWORD(StoneSword),
    STONE_AXE_HEAD(StoneAxeHead),
    STONE_AXE(StoneAxe),

    IRON(Iron),
    IRON_BLADE(IronBlade),
    IRON_SWORD(IronSword),

    DIAMOND(Diamond),
    DIAMOND_BLADE(DiamondBlade),
    DIAMOND_SWORD(DiamondSword);

    companion object {
        fun emptyMap() = values().associateWithTo(EnumMap(Reagent::class.java)) { 0 }

        val droppable = values().filter { it.isDroppable() }
        val damaging = values().filter { it.isDamaging() }
        val craftable = values().filter { it.isCraftable() }
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
    fun asDamageable(): DamagingReagent {
        return data as DamagingReagent
    }
    fun asCraftable(): CraftableReagent {
        return data as CraftableReagent
    }
    fun asDroppable(): DroppableReagent {
        return data as DroppableReagent
    }
}