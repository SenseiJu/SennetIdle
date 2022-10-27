package me.senseiju.sennetidle.reagents

import me.mattstudios.mf.base.components.TypeResult
import me.senseiju.sennetidle.plugin
import me.senseiju.sentils.service.Service
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.trait.TraitInfo

class ReagentService : Service() {

    init {
        plugin.commandManager.parameterHandler.register(Reagent::class.java) { argument ->
            return@register try {
                TypeResult(Reagent.valueOf(argument.toString().uppercase()), argument)
            } catch (ex: IllegalArgumentException) {
                TypeResult(argument)
            }
        }

        plugin.commandManager.register(ReagentCommand())

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ReagentTrait::class.java))
    }
}