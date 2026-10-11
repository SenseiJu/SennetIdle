package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object Staff : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 6
    override val waveUnlock = 301
    override val name = "Staff"
    override val material = Material.STICK
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.WAND to 1, Reagent.IRON to 1, Reagent.MAGIC_CORE to 1)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}