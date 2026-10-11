package gg.idlemob.idlemobcore.blueprint

import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.framework.extensions.formatDuration
import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import gg.idlemob.idlemobcore.framework.text.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class BlueprintItem(val id: String, val uses: Int, val uuid: UUID)

class BlueprintItems(plugin: IdleMobCore, private val blueprints: Blueprints) {
    private val idKey = NamespacedKey(plugin, "blueprint")
    private val usesKey = NamespacedKey(plugin, "blueprint_uses")
    private val uuidKey = NamespacedKey(plugin, "blueprint_uuid")

    fun create(id: String, uses: Int, uuid: UUID = UUID.randomUUID()): ItemStack {
        val blueprint = blueprints[id] ?: throw IllegalArgumentException("$id doesn't have a blueprint")

        return blueprint.resource.display.toItemStack().apply {
            editMeta {
                it.displayName(mini(blueprint.name))
                it.lore(lore(blueprint, uses))
                it.setEnchantmentGlintOverride(true)
                it.setMaxStackSize(1)
                it.persistentDataContainer.set(idKey, PersistentDataType.STRING, id)
                it.persistentDataContainer.set(usesKey, PersistentDataType.INTEGER, uses)
                it.persistentDataContainer.set(uuidKey, PersistentDataType.STRING, uuid.toString())
            }
        }
    }

    fun read(item: ItemStack?): BlueprintItem? {
        if (item == null || item.isEmpty) {
            return null
        }

        val data = item.persistentDataContainer
        val id = data.get(idKey, PersistentDataType.STRING) ?: return null
        val uses = data.get(usesKey, PersistentDataType.INTEGER) ?: return null
        val uuid = data.get(uuidKey, PersistentDataType.STRING)?.let(UUID::fromString) ?: return null
        return BlueprintItem(id, uses, uuid)
    }

    private fun lore(blueprint: Blueprint, uses: Int): List<Component> = listOf(
        Component.empty(),
        mini("<gold><b>BLUEPRINT"),
        mini(
            "<dark_gray>| <gray>Makes: <white><amount>x</white> <resource>",
            Placeholder.unparsed("amount", blueprint.amount.formatSuffixed()),
            Placeholder.parsed("resource", blueprint.outputName),
        ),
        mini("<dark_gray>| <gray>Craft time: <white><time>", Placeholder.unparsed("time", blueprint.craftTime.toMillis().formatDuration())),
        mini("<dark_gray>| <gray>Uses left: <white><uses>", Placeholder.unparsed("uses", uses.toString())),
        Component.empty(),
        mini("<yellow>Right-click a generator to use it"),
    )
}
