package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object WoodenSword : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 0
    override val waveUnlock = 1
    override val name = "Wooden Sword"
    override val material = Material.WOODEN_SWORD
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.WOODEN_HANDLE to 1, Reagent.WOODEN_BLADE to 1)
    override val damagePerSecond = 15
    override val bossOnly = false
    override val amountPerCraft = 1
}