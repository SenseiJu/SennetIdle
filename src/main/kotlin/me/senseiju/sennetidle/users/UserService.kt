package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.idlemobs.IdleMobService
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.utils.ioScope
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import java.util.*

class UserService(plugin: SennetIdle) : Service() {
    private val regentService = serviceProvider.get<ReagentService>()

    private val userSaveHandler = UserSaveHandler()
    private val userLoadHandler = UserLoadHandler(plugin)

    val users = hashMapOf<UUID, User>()

    //region Service

    init {
        plugin.registerEvents(UserConnectListener(this))

        UserSaveTask(plugin, this)
    }

    override fun onDisable() {
        saveUsers(false)
    }

    //endregion

    fun getUser(uuid: UUID) = users.getOrPut(uuid) { createEmptyUser(uuid) }

    fun saveUser(user: User, async: Boolean) {
        if (async) {
            ioScope { userSaveHandler.saveUser(user) }
        } else {
            userSaveHandler.saveUser(user)
        }
    }

    fun saveUsers(async: Boolean = true) {
        users.values.forEach { saveUser(it, async) }
    }

    fun createUser(uuid: UUID) {
        val user = createEmptyUser(uuid)

        userLoadHandler.loadUser(user)

        users[uuid] = user
    }

    private fun createEmptyUser(uuid: UUID) = User(uuid, emptyUserRegentsMap(), 0)

    private fun emptyUserRegentsMap() = regentService.reagents.values.associateTo(hashMapOf()) { it.id to UserReagent(it.id) }
}