package gg.idlemob.idlemobcore.box

import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.framework.text.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class BoxItems(plugin: IdleMobCore, private val boxes: BoxRegistry) {
    private val boxKey = NamespacedKey(plugin, "box")

    fun create(id: String, amount: Int): ItemStack {
        val box = boxes[id] ?: throw IllegalArgumentException("Unknown box $id")

        return box.display.toItemStack().apply {
            this.amount = amount
            editMeta {
                it.displayName(box.display.displayName())
                it.lore(lore(box))
                it.setEnchantmentGlintOverride(true)
                it.persistentDataContainer.set(boxKey, PersistentDataType.STRING, id)
            }
        }
    }

    fun boxId(item: ItemStack?): String? {
        if (item == null || item.isEmpty) {
            return null
        }
        return item.persistentDataContainer.get(boxKey, PersistentDataType.STRING)
    }

    private fun lore(box: BoxConfig): List<Component> {
        val lore = mutableListOf<Component>()

        if (box.guaranteedRewards.isNotEmpty()) {
            lore += Component.empty()
            lore += mini("<green><b>GUARANTEED")
            box.guaranteedRewards.forEach {
                lore += mini("<dark_gray>| <reward>", Placeholder.parsed("reward", it.name))
            }
        }

        if (box.randomRewards.isNotEmpty()) {
            val totalWeight = box.randomRewards.sumOf { it.weight }

            lore += Component.empty()
            lore += mini("<aqua><b>ONE OF")
            box.randomRewards.forEach {
                lore += mini(
                    "<dark_gray>| <reward> <dark_gray>(<chance>%)",
                    Placeholder.parsed("reward", it.name),
                    Placeholder.unparsed("chance", formatChance(it.weight / totalWeight * 100)),
                )
            }
        }

        box.merge?.let { merge ->
            lore += Component.empty()
            lore += mini("<light_purple><b>MERGE")
            lore += mini(
                "<dark_gray>| <gray><amount> of these merge into 1 <into>",
                Placeholder.unparsed("amount", merge.amount.toString()),
                Placeholder.parsed("into", boxes[merge.into]?.display?.name ?: merge.into),
            )
            lore += mini("<dark_gray>| <gray>Use <yellow>/boxes merge")
        }

        lore += Component.empty()
        lore += mini("<yellow>Right-click to open")
        lore += mini("<yellow>Sneak + right-click to open all")

        return lore
    }

    private fun formatChance(chance: Double): String {
        val formatted = String.format(Locale.ROOT, if (chance >= 1) "%.1f" else "%.2f", chance)
        return formatted.trimEnd('0').trimEnd('.')
    }
}
