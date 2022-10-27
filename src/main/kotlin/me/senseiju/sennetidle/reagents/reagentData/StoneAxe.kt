package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object StoneAxe : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 0
    override val name = "Stone Axe"
    override val material = Material.STONE_AXE
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.WOODEN_HANDLE to 1, Reagent.STONE_AXE_HEAD to 1)
    override val damagePerSecond = 15
    override val bossOnly = true
    override val amountPerCraft = 1
}