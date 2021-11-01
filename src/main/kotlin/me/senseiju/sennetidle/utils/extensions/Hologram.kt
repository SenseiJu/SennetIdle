package me.senseiju.sennetidle.utils.extensions

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun Hologram.appendItemLine(material: Material): ItemLine {
    return appendItemLine(ItemStack(material))
}