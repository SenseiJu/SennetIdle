package me.senseiju.sennetidle.reagents.reagentData

import me.senseiju.sennetidle.reagents.Reagent
import org.bukkit.Material

object NetheriteBlade : CraftableReagent, DamagingReagent {
    override val promotionUnlock = 7
    override val waveUnlock = 351
    override val name = "Netherite Blade"
    override val material = Material.NETHERITE_SWORD
    override val modelData = 0
    override val reagentRequirements = mapOf(Reagent.NETHERITE to 4)
    override val damagePerSecond = 4335
    override val bossOnly = false
    override val amountPerCraft = 1
}