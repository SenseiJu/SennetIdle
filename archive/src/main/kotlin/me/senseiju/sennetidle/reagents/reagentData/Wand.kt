package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object Wand : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 5
    override val waveUnlock = 281
    override val name = "Wand"
    override val material = Material.STICK
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.WOOD to 1, Reagent.MAGIC_CORE to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}