package me.senseiju.sennetidle.upgrades

import me.mattstudios.mf.base.components.TypeResult
import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.service.Service
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.trait.TraitInfo

class UpgradeService : Service() {
    init {
        plugin.commandManager.parameterHandler.register(Upgrade::class.java) { argument ->
            return@register try {
                TypeResult(Upgrade.valueOf(argument.toString().uppercase()), argument)
            } catch (ex: IllegalArgumentException) {
                TypeResult(argument)
            }
        }

        plugin.commandManager.register(UpgradeCommand())

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(UpgradeTrait::class.java))
    }
}