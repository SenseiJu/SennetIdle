package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object NetheriteSword : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 7
    override val waveUnlock = 351
    override val name = "Netherite Sword"
    override val material = Material.NETHERITE_SWORD
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.NETHERITE_BLADE to 1, Reagent.WOODEN_HANDLE to 3, Reagent.DIAMOND_SWORD to 9)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}