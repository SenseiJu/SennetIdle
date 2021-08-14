package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.database
import me.senseiju.sennetidle.database.UserIdleMobTable
import me.senseiju.sennetidle.database.UserReagentsTable
import me.senseiju.sennetidle.idlemobs.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sentils.runnables.newRunnable
import org.ktorm.dsl.*

class UserLoadHandler(private val plugin: SennetIdle) {
    private val idleMobService = serviceProvider.get<IdleMobService>()

    fun loadUser(user: User) {
        loadUserRegents(user)
        loadUserIdleMob(user)
    }

    private fun loadUserRegents(user: User) {
        database.from(UserReagentsTable)
            .select()
            .where(UserReagentsTable.userUUID eq user.uuid.toString())
            .forEach {
                user.reagents[it[UserReagentsTable.reagentId]!!] =
                    UserReagent(
                        it[UserReagentsTable.reagentId]!!,
                        it[UserReagentsTable.reagentTotalAmountCrafted]!!,
                        it[UserReagentsTable.reagentAmount]!!
                    )
            }
    }

    private fun loadUserIdleMob(user: User) {
        database.from(UserIdleMobTable)
            .select()
            .where(UserIdleMobTable.userUUID eq user.uuid.toString())
            .forEach {
                user.currentWave = it[UserIdleMobTable.currentWave]!!
            }

        newRunnable {
            user.idleMob = idleMobService.createIdleMob(user)
        }.runTask(plugin)
    }
}