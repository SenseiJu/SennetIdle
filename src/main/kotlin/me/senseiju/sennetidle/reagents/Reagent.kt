package me.senseiju.sennetidle.reagents

import me.senseiju.sennetidle.reagents.reagentData.*
import me.senseiju.sennetidle.reagents.reagentData.String
import java.util.*

enum class Reagent(val data: BaseReagent) {
    WOOD(Wood),
    STRING(String), // TODO
    STONE(Stone),
    GOLD(Gold), // TODO
    IRON(Iron),
    DIAMOND(Diamond),
    NETHERITE(Netherite), // TODO
    NEBULA(Nebula), // TODO

    WOODEN_HANDLE(WoodenHandle),
    WOODEN_BLADE(WoodenBlade),
    WOODEN_SWORD(WoodenSword),

    STONE_BLADE(StoneBlade),
    STONE_SWORD(StoneSword),

    GOLDEN_BLADE(GoldenBlade), // TODO
    GOLDEN_SWORD(GoldenSword), // TODO

    IRON_BLADE(IronBlade),
    IRON_SWORD(IronSword),

    DIAMOND_BLADE(DiamondBlade),
    DIAMOND_SWORD(DiamondSword),

    NETHERITE_BLADE(NetheriteBlade), // TODO
    NETHERITE_SWORD(NetheriteSword), // TODO

    SLINGSHOT(Slingshot), // TODO
    BOW(Bow), // TODO
    COMPOUND_BOW(CompoundBow), // TODO
    CROSSBOW(Crossbow), // TODO

    MAGIC_CORE(MagicCore), // TODO
    WAND(Wand), // TODO
    STAFF(Staff), // TODO

    TOY_PIECES(ToyPieces), // TODO
    TOY_SOLDIER(ToySoldier), // TODO
    ARMORED_TOY_SOLDIER(ArmoredToySoldier), // TODO

    ;

    companion object {
        fun emptyMap() = values().associateWithTo(EnumMap(Reagent::class.java)) { 0 }

        val ordered = values().sortedBy { it.data.waveUnlock }
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