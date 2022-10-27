package me.senseiju.sennetidle.upgrades

import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitName
import org.bukkit.event.EventHandler

@TraitName("upgradegui")
class UpgradeTrait : Trait("upgradegui") {

    @EventHandler
    fun onClick(e: NPCRightClickEvent) {
        e.clicker.openUpgradeGui()
    }
}