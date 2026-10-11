package gg.idlemob.idlemobcore.framework.config.types

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class ItemDisplay {
    var name = "<white>Unnamed"
    var material = Material.PAPER
    var itemModel: NamespacedKey = NamespacedKey.minecraft("paper")

    fun displayName(): Component {
        return MiniMessage.miniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false)
    }

    fun toItemStack(): ItemStack {
        return ItemStack.of(material).apply {
            editMeta {
                it.displayName(displayName())
                it.itemModel = itemModel
            }
            setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS).build())
        }
    }
}
