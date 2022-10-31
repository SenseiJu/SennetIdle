package me.senseiju.sennetidle.utils

import dev.triumphteam.gui.builder.item.ItemBuilder
import me.senseiju.sennetidle.utils.extensions.component
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

interface ModelItem {
    val name: String
    val material: Material
    val modelData: Int

    fun itemStack(): ItemStack {
        return ItemBuilder.from(material)
            .name(name.component())
            .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE)
            .apply {
                if (modelData > 0) {
                    model(modelData)
                }
            }.build()
    }
}