package gg.idlemob.idlemobcore.resource.menu

import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.resource.DamageTarget
import gg.idlemob.idlemobcore.resource.ResourceConfig
import gg.idlemob.idlemobcore.resource.ResourceRegistry
import gg.idlemob.idlemobcore.resource.isUnlockedFor
import gg.idlemob.idlemobcore.user.User
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

class ResourceLore(private val resources: ResourceRegistry) {

    fun lore(user: User, id: String, resource: ResourceConfig, showClickHint: Boolean = false, includeRecipe: Boolean = true): List<Component> {
        val lore = mutableListOf<Component>()
        val owned = user.getResource(id)

        lore += quantity(owned)
        resource.components.damaging?.let {
            lore += mini("<gray>Total DPS: <white><dps>", Placeholder.unparsed("dps", (it.dps * owned).formatSuffixed()))
        }

        resource.components.damaging?.let {
            lore += Component.empty()
            lore += mini("<red><b>DAMAGING")
            lore += mini("<dark_gray>| <gray>DPS: <white><dps></white> each", Placeholder.unparsed("dps", it.dps.formatSuffixed()))
            when (it.target) {
                DamageTarget.MOBS -> lore += mini("<dark_gray>| <gray>Only damages normal mobs")
                DamageTarget.BOSSES -> lore += mini("<dark_gray>| <gray>Only damages bosses")
                DamageTarget.ALL -> {}
            }
        }

        resource.components.droppable?.let {
            lore += Component.empty()
            lore += mini("<aqua><b>DROPPABLE")
            if (it.mob != null) lore += mini("<dark_gray>| <gray>Drops from mobs")
            if (it.boss != null) lore += mini("<dark_gray>| <gray>Drops from bosses")
        }

        if (!resource.isUnlockedFor(user)) {
            lore += Component.empty()
            lore += mini("<dark_red><b>LOCKED")
            lore += mini(
                "<dark_gray>| <gray>Needs stage <white><stage></white> and prestige <white><prestige>",
                Placeholder.unparsed("stage", resource.unlock.stage.toString()),
                Placeholder.unparsed("prestige", resource.unlock.prestige.toString()),
            )
            return lore
        }

        resource.components.craftable?.takeIf { includeRecipe }?.let { craftable ->
            lore += Component.empty()
            lore += mini("<gold><b>CRAFTABLE")
            craftable.ingredients.forEach { (ingredient, needed) ->
                lore += ingredient(ingredient, needed.toDouble(), user)
            }
            lore += mini("<dark_gray>| <gray>Crafts <white><output>", Placeholder.unparsed("output", craftable.output.toString()))

            if (showClickHint) {
                lore += Component.empty()
                lore += mini("<yellow>Click to craft")
            }
        }

        return lore
    }

    fun quantity(amount: Double): Component {
        return mini("<gray>Quantity: <white><amount>", Placeholder.unparsed("amount", amount.formatSuffixed()))
    }

    fun ingredient(id: String, needed: Double, user: User, available: Boolean = true): Component {
        val have = user.getResource(id)
        val color = if (available && have >= needed) "<green>" else "<red>"
        return mini(
            "<dark_gray>| $color<have>/<needed> <name>",
            Placeholder.parsed("name", resources[id]?.display?.name ?: id),
            Placeholder.unparsed("have", have.formatSuffixed()),
            Placeholder.unparsed("needed", needed.formatSuffixed()),
        )
    }
}
