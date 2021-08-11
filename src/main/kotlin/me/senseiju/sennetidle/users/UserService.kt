package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.database
import me.senseiju.sennetidle.database.UserRegentsTable
import me.senseiju.sennetidle.database.UserRegentsTable.regentAmount
import me.senseiju.sennetidle.database.UserRegentsTable.regentId
import me.senseiju.sennetidle.database.UserRegentsTable.regentLevel
import me.senseiju.sennetidle.database.UserRegentsTable.userUUID
import me.senseiju.sennetidle.regents.RegentService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.utils.ioScope
import me.senseiju.sentils.registerEvents
import me.senseiju.sentils.service.Service
import org.ktorm.dsl.*
import org.ktorm.support.mysql.insertOrUpdate
import java.util.*

class UserService(plugin: SennetIdle) : Service() {
    private val regentService = serviceProvider.get<RegentService>()

    val users = hashMapOf<UUID, User>()

    init {
        plugin.registerEvents(UserListener(this))
    }

    override fun onDisable() {
        saveUsers(false)
    }

    fun saveUsers(async: Boolean = true) {
        users.values.forEach { user ->
            if (async) {
                ioScope { saveUserRegents(user) }
            } else {
                saveUserRegents(user)
            }
        }
    }

    private fun saveUserRegents(user: User) {
        user.regents.values.forEach { userRegent ->
            database.insertOrUpdate(UserRegentsTable) {
                set(userUUID, user.uuid)
                set(regentId, userRegent.id)
                set(regentAmount, userRegent.amount)
                set(regentLevel, userRegent.level)
                onDuplicateKey {
                    set(regentAmount, userRegent.amount)
                    set(regentLevel, userRegent.level)
                }
            }
        }
    }

    fun createUser(uuid: UUID) {
        val user = User(uuid, emptyUserRegentsMap())

        database.from(UserRegentsTable)
            .select()
            .where(userUUID eq uuid)
            .forEach {
                user.regents[it[regentId]!!] =
                    UserRegent(
                        it[regentId]!!,
                        it[regentLevel]!!,
                        it[regentAmount]!!
                    )
            }

        users[uuid] = user
    }

    private fun emptyUserRegentsMap() = regentService.regents.values.associateTo(hashMapOf()) { it.id to UserRegent(it.id) }
}