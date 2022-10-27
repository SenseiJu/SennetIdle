package me.senseiju.sennetidle.reagents

import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitName
import org.bukkit.event.EventHandler

@TraitName("reagentgui")
class ReagentTrait : Trait("reagentgui") {

    @EventHandler
    fun onClick(e: NPCRightClickEvent) {
        e.clicker.openReagentGui()
    }
}