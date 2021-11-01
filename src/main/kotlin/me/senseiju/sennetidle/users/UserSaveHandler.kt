package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.crafting.CraftingService
import me.senseiju.sennetidle.database
import me.senseiju.sennetidle.database.KeyedReplacements
import me.senseiju.sennetidle.database.tables.UserActiveGeneratorsTable
import me.senseiju.sennetidle.database.tables.UserIdleMobTable
import me.senseiju.sennetidle.database.tables.UserMetaDataTable
import me.senseiju.sennetidle.database.tables.UserReagentsTable
import me.senseiju.sennetidle.serviceProvider
import org.bukkit.Keyed

private val craftingService = serviceProvider.get<CraftingService>()

object UserSaveHandler {
    fun saveUser(user: User) {
        saveUserReagents(user)
        saveUserIdleMob(user)
        saveUserCraftingStations(user)
        saveUserMetaData(user)
    }

    private fun saveUserReagents(user: User) {
        val replacements = user.reagents.values.map {
            KeyedReplacements(
                arrayOf(user.uuid.toString(), it.id),
                it.totalAmountCrafted, it.amount
            )
        }

        database.updateBatch(UserReagentsTable.UPDATE_USER_REAGENTS_QUERY, replacements)
    }

    private fun saveUserIdleMob(user: User) {
        val replacements = KeyedReplacements(arrayOf(user.uuid.toString()), user.currentWave)

        database.update(UserIdleMobTable.UPDATE_USER_IDLE_MOB_QUERY, replacements)
    }

    private fun saveUserCraftingStations(user: User) {
        val replacements = craftingService.getUserCraftingStations(user).map {
            KeyedReplacements(arrayOf(user.uuid.toString(), it.key), it.value.getActiveReagent()?.id ?: "")
        }

        database.updateBatch(UserActiveGeneratorsTable.UPDATE_USER_CRAFTING_STATIONS, replacements)
    }

    private fun saveUserMetaData(user: User) {
        val replacements = KeyedReplacements(arrayOf(user.uuid.toString()), user.lastSeen)

        database.update(UserMetaDataTable.UPDATE_USER_META_DATA, replacements)
    }
}