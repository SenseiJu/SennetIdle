package gg.idlemob.idlemobcore.framework.extensions

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun Player.giveOrDrop(items: Collection<ItemStack>) {
    inventory.addItem(*items.toTypedArray()).values.forEach { world.dropItemNaturally(location, it) }
}
