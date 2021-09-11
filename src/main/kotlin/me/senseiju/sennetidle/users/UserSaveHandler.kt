package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.database
import me.senseiju.sennetidle.database.UserActiveGeneratorsTable
import me.senseiju.sennetidle.database.UserIdleMobTable
import me.senseiju.sennetidle.database.UserReagentsTable
import me.senseiju.sennetidle.generator.GeneratorService
import me.senseiju.sennetidle.serviceProvider
import org.ktorm.support.mysql.insertOrUpdate

private val generatorService = serviceProvider.get<GeneratorService>()

object UserSaveHandler {

    fun saveUser(user: User) {
        saveUserReagents(user)
        saveUserIdleMob(user)
        saveUserActiveGenerators(user)
    }

    private fun saveUserReagents(user: User) {
        user.reagents.values.forEach { userRegent ->
            database.insertOrUpdate(UserReagentsTable) {
                set(it.userUUID, user.uuid.toString())
                set(it.reagentId, userRegent.id)
                set(it.reagentAmount, userRegent.amount)
                set(it.reagentTotalAmountCrafted, userRegent.totalAmountCrafted)
                onDuplicateKey {
                    set(it.reagentAmount, userRegent.amount)
                    set(it.reagentTotalAmountCrafted, userRegent.totalAmountCrafted)
                }
            }
        }
    }

    private fun saveUserIdleMob(user: User) {
        database.insertOrUpdate(UserIdleMobTable) {
            set(it.userUUID, user.uuid.toString())
            set(it.currentWave, user.currentWave)
            onDuplicateKey {
                set(it.currentWave, user.currentWave)
            }
        }
    }

    private fun saveUserActiveGenerators(user: User) {
        generatorService.generators.map {
            it.key to it.value.activeUserCrafts[user]?.reagent?.id
        }.forEach { (generatorId, reagentId) ->
            database.insertOrUpdate(UserActiveGeneratorsTable) {
                set(it.userUUID, user.uuid.toString())
                set(it.generatorId, generatorId)
                set(it.reagentId, reagentId)
                onDuplicateKey {
                    set(it.reagentId, reagentId)
                }
            }
        }
    }
}