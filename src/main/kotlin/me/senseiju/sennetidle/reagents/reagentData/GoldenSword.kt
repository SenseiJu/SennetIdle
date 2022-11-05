package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object GoldenSword : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 0
    override val name = "Golden Sword"
    override val material = Material.GOLDEN_SWORD
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.GOLDEN_BLADE to 1, Reagent.WOODEN_HANDLE to 3, Reagent.STONE_SWORD to 9)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}