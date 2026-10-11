package gg.idlemob.idlemobcore.framework.menu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.Function

fun dynamic(provider: () -> ItemProvider): Item {
    return Item.builder()
        .setItemProvider(Function<Player, ItemProvider> { provider() })
        .updatePeriodically(20)
        .build()
}

fun button(material: Material, name: Component): ItemBuilder {
    return ItemBuilder(material).setName(name.decoration(TextDecoration.ITALIC, false))
}

fun filler(): ItemBuilder {
    return ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)
}
