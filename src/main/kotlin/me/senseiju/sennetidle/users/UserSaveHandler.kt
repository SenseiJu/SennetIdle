package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.database
import me.senseiju.sennetidle.database.UserIdleMobTable
import me.senseiju.sennetidle.database.UserReagentsTable
import org.ktorm.support.mysql.insertOrUpdate

class UserSaveHandler {

    fun saveUser(user: User) {
        saveUserReagents(user)
        saveUserIdleMob(user)
    }

    private fun saveUserReagents(user: User) {
        user.reagents.values.forEach { userRegent ->
            database.insertOrUpdate(UserReagentsTable) {
                set(UserReagentsTable.userUUID, user.uuid.toString())
                set(UserReagentsTable.reagentId, userRegent.id)
                set(UserReagentsTable.reagentAmount, userRegent.amount)
                set(UserReagentsTable.reagentTotalAmountCrafted, userRegent.totalAmountCrafted)
                onDuplicateKey {
                    set(UserReagentsTable.reagentAmount, userRegent.amount)
                    set(UserReagentsTable.reagentTotalAmountCrafted, userRegent.totalAmountCrafted)
                }
            }
        }
    }

    private fun saveUserIdleMob(user: User) {
        database.insertOrUpdate(UserIdleMobTable) {
            set(UserIdleMobTable.userUUID, user.uuid.toString())
            set(UserIdleMobTable.currentWave, user.currentWave)
            onDuplicateKey {
                set(UserIdleMobTable.currentWave, user.currentWave)
            }
        }
    }
}