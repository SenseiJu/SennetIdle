package me.senseiju.sennetidle.inventory

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.inventory.listeners.PlayerInventoryListener
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import java.util.*

class InventoryService(plugin: SennetIdle) : Service() {
    val inventoryLockBypass = hashSetOf<UUID>()

    init {
        plugin.registerEvents(PlayerInventoryListener(this))
    }
}