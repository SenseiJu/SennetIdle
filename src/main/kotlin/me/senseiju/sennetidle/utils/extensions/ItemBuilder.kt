package me.senseiju.sennetidle.utils.extensions

import dev.triumphteam.gui.builder.item.ItemBuilder
import net.kyori.adventure.text.Component

fun ItemBuilder.addLore(vararg components: Component): ItemBuilder {
    lore {
        it.addAll(components)
    }

    return this
}

fun ItemBuilder.addLore(components: List<Component>): ItemBuilder {
    lore {
        it.addAll(components)
    }

    return this
}