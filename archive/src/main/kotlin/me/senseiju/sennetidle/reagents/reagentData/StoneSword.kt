package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object StoneSword : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 1
    override val waveUnlock = 51
    override val name = "Stone Sword"
    override val material = Material.STONE_SWORD
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.STONE_BLADE to 1, Reagent.WOODEN_HANDLE to 2, Reagent.WOODEN_SWORD to 9)
    override val damagePerSecond = 255
    override val bossOnly = false
    override val amountPerCraft = 1
}