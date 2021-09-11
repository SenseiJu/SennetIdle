package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.utils.ioScope
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import java.util.*

private val reagentService = serviceProvider.get<ReagentService>()

class UserService(plugin: SennetIdle) : Service() {
    val users = hashMapOf<UUID, User>()

    //region Service

    init {
        plugin.registerEvents(UserConnectListener(this))

        UserSaveTask(plugin)
    }

    override fun onDisable() {
        saveUsers(false)
    }

    //endregion

    fun getUser(uuid: UUID) = users.getOrPut(uuid) { createEmptyUser(uuid) }

    fun saveUser(user: User, async: Boolean) {
        if (async) {
            ioScope { UserSaveHandler.saveUser(user) }
        } else {
            UserSaveHandler.saveUser(user)
        }
    }

    fun saveUsers(async: Boolean = true) {
        users.values.forEach { saveUser(it, async) }
    }

    fun createUser(uuid: UUID): User {
        val user = createEmptyUser(uuid)
        UserLoadHandler.loadUser(user)
        users[uuid] = user
        return user
    }

    private fun createEmptyUser(uuid: UUID) = User(uuid, emptyUserReagentsMap(), 1)

    private fun emptyUserReagentsMap() = reagentService.reagents.values.associateTo(hashMapOf()) { it.id to UserReagent(it.id) }
}