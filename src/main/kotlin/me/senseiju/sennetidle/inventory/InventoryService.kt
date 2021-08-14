package me.senseiju.sennetidle.inventory

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.inventory.listeners.PlayerInventoryListener
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service

class InventoryService(plugin: SennetIdle) : Service() {

    init {
        plugin.registerEvents(PlayerInventoryListener())
    }
}