package me.senseiju.sennetidle.powers

import me.senseiju.sennetidle.plugin
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

private val userService = serviceProvider.get<UserService>()

class PowerService : Service(), Listener {

    init {
        plugin.registerEvents(this)
    }

    @EventHandler
    private fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        repeat(9) {
            e.player.inventory.setItem(it, ItemStack(Material.AIR))
        }

        Power.values().map { it.data }.forEach {
            e.player.inventory.setItem(it.slot, it.itemStack())
        }
    }

    @EventHandler
    private fun onInventoryInteractEvent(e: InventoryClickEvent) {
        e.isCancelled = true
    }

    @EventHandler
    private fun onPlayerDropItemEvent(e: PlayerDropItemEvent) {
        e.isCancelled = true
    }

    @EventHandler
    private fun onRightClickEvent(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) {
            return
        }

        if (e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        Power.values().map { it.data }.forEach {
            if (e.player.inventory.heldItemSlot == it.slot) {
                if (e.player.inventory.itemInMainHand.itemMeta.persistentDataContainer.get(Power.namespacedKey, PersistentDataType.STRING) == it.name) {
                    it.onRightClick(userService.getUser(e.player.uniqueId))
                    return
                }
            }
        }
    }
}