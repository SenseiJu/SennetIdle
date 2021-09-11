package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.idlemobs.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sentils.CleanupTask

private val userService = serviceProvider.get<UserService>()
private val idleService = serviceProvider.get<IdleMobService>()

class UserSaveTask(
    private val plugin: SennetIdle
) : CleanupTask() {

    override fun cleanup() {
        userService.users.forEach { (uuid, user) ->
            val player = plugin.server.getPlayer(uuid) ?: return@forEach

            if (userService.users.containsKey(player.uniqueId)) {
                userService.saveUser(user, false)
            }

            if (!player.isOnline) {
                idleService.disposeWaveHandler(user.uuid)
                userService.users.remove(user.uuid)
            }
        }
    }
}