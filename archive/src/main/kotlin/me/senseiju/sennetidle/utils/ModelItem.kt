package me.senseiju.sennetidle.utils

import me.senseiju.sennetidle.utils.extensions.component
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

interface ModelItem {
    val name: String
    val material: Material
    val modelData: Int

    fun itemStack(): ItemStack {
        return ItemStack.of(material).apply {
            editMeta { meta ->
                meta.displayName(name.component())
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE)

                if (modelData > 0) {
                    meta.setCustomModelDataComponent(meta.customModelDataComponent.apply {
                        floats = listOf(modelData.toFloat())
                    })
                }
            }
        }
    }
}
