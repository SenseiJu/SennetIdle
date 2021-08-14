package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sentils.CleanupTask

class UserSaveTask(
    private val plugin: SennetIdle,
    private val userService: UserService
) : CleanupTask() {

    override fun cleanup() {
        userService.users.forEach { (uuid, user) ->
            val player = plugin.server.getPlayer(uuid) ?: return@forEach

            if (userService.users.containsKey(player.uniqueId)) {
                userService.saveUser(user, false)
            }

            if (!player.isOnline) {
                user.idleMob.dispose()

                userService.users.remove(user.uuid)
            }
        }
    }
}