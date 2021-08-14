package me.senseiju.sennetidle.inventory.listeners

import dev.triumphteam.gui.builder.item.ItemBuilder
import me.senseiju.sennetidle.reagents.openReagentsGui
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.UserService
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class PlayerInventoryListener : Listener {
    private val userService = serviceProvider.get<UserService>()

    @EventHandler
    private fun onPlayerJoin(e: PlayerJoinEvent) {
        e.player.inventory.clear()

        applyInventory(e.player.inventory)
    }

    @EventHandler
    private fun onItemDrop(e: PlayerDropItemEvent) {
        e.isCancelled = true
    }

    @EventHandler
    private fun onInventoryClick(e: InventoryClickEvent) {
        if (e.clickedInventory !is PlayerInventory) return

        e.isCancelled = true
    }

    @EventHandler
    private fun onInventoryPickupItem(e: InventoryPickupItemEvent) {
        if (e.inventory !is PlayerInventory) return

        e.isCancelled = true
    }

    @EventHandler
    private fun onInventoryDrag(e: InventoryDragEvent) {
        if (e.inventory !is PlayerInventory) return

        e.isCancelled = true
    }

    @EventHandler
    private fun onInventoryMoveItem(e: InventoryMoveItemEvent) {
        if (e.initiator !is PlayerInventory && e.destination !is PlayerInventory) return

        e.isCancelled = true
    }

    @EventHandler
    private fun onSwapHandItems(e: PlayerSwapHandItemsEvent) {
        e.isCancelled = true
    }

    @EventHandler
    private fun onRightClickRegentsItem(e: PlayerInteractEvent) {
        if (e.hand == EquipmentSlot.OFF_HAND) return

        val user = userService.getUser(e.player.uniqueId)

        if (e.item == regentsItem) {
            e.player.openReagentsGui(user)
        }
    }

    private fun applyInventory(playerInventory: PlayerInventory) {
        playerInventory.setItem(1, regentsItem)
        playerInventory.setItem(4, ItemStack(Material.WOODEN_SWORD))
    }
}

private val regentsItem = ItemBuilder.from(Material.CHEST)
    .name(text("Reagents", RED))
    .build()