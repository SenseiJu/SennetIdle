package gg.idlemob.idlemobcore.fight

import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.framework.extensions.giveOrDrop
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class StashRestore(plugin: IdleMobCore) : Listener {
    private val stashKey = NamespacedKey(plugin, "stashed_inventory")

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val stash = player.persistentDataContainer.get(stashKey, PersistentDataType.BYTE_ARRAY) ?: return

        val extras = player.inventory.contents.filterNotNull().filter { !it.isEmpty && !it.itemMeta.isUnbreakable }
        player.inventory.contents = ItemStack.deserializeItemsFromBytes(stash)
        player.persistentDataContainer.remove(stashKey)
        player.giveOrDrop(extras)

        EventLogger.info("stashed_inventory_restored", player) { "extras" to extras.size }
    }
}
