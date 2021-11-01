package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.crafting.CraftingService
import me.senseiju.sennetidle.idlemobs.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sentils.CleanupTask

private val userService = serviceProvider.get<UserService>()
private val idleService = serviceProvider.get<IdleMobService>()
private val craftingService = serviceProvider.get<CraftingService>()

class UserSaveTask : CleanupTask() {

    override fun cleanup() {
        userService.users.values.forEach { user ->
            val player = user.getBukkitPlayer() ?: return@forEach

            userService.saveUser(user, false)

            if (!player.isOnline) {
                idleService.disposeWaveHandler(user.uuid)
                userService.users.remove(user.uuid)
                craftingService.disposeUserCraftingStations(user)
            }
        }
    }
}