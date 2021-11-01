package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.crafting.CraftingService
import me.senseiju.sennetidle.database
import me.senseiju.sennetidle.database.tables.UserActiveGeneratorsTable
import me.senseiju.sennetidle.database.tables.UserIdleMobTable
import me.senseiju.sennetidle.database.tables.UserMetaDataTable
import me.senseiju.sennetidle.database.tables.UserReagentsTable
import me.senseiju.sennetidle.reagents.ReagentService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.utils.extensions.forEach
import me.senseiju.sentils.runnables.newRunnable
import org.bukkit.plugin.java.JavaPlugin

private val plugin = JavaPlugin.getPlugin(SennetIdle::class.java)

private val reagentService = serviceProvider.get<ReagentService>()
private val craftingService = serviceProvider.get<CraftingService>()

object UserLoadHandler {

    fun loadUser(user: User) {
        loadUserRegents(user)
        loadUserIdleMob(user)
        loadUserCraftingStations(user)
        loadUserMetaData(user)
    }

    private fun loadUserRegents(user: User) {
        val results = database.query(UserReagentsTable.SELECT_USER_REAGENTS_QUERY, user.uuid.toString())

        results.forEach {
            user.reagents[it.getString(UserReagentsTable.reagentId)] =
                UserReagent(
                    it.getString(UserReagentsTable.reagentId),
                    it.getLong(UserReagentsTable.reagentTotalAmountCrafted),
                    it.getLong(UserReagentsTable.reagentAmount)
                )
        }

        user.recalculatePassiveDPS()
    }

    private fun loadUserIdleMob(user: User) {
        val results = database.query(UserIdleMobTable.SELECT_USER_IDLE_MOB_QUERY, user.uuid.toString())

        results.forEach {
            user.currentWave = it.getInt(UserIdleMobTable.currentWave)
        }
    }

    private fun loadUserCraftingStations(user: User) {
        val results = database.query(UserActiveGeneratorsTable.SELECT_USER_CRAFTING_STATIONS, user.uuid.toString())

        results.forEach {
            val craftingStationHandler = craftingService.getCraftingStationHandler(it.getString(UserActiveGeneratorsTable.generatorId)) ?: return@forEach
            val reagent = reagentService.reagents[it.getString(UserActiveGeneratorsTable.reagentId)]

            newRunnable {
                val userCraftingStation = craftingStationHandler.addNewUserStation(user)
                if (reagent != null) {
                    userCraftingStation.setActiveCraftingReagent(craftingService.getNextCraftingReagent(reagent, user))
                }
            }.runTask(plugin)
        }
    }

    private fun loadUserMetaData(user: User) {
        val results = database.query(UserMetaDataTable.SELECT_USER_META_DATA, user.uuid.toString())

        results.forEach {
            user.lastSeen = it.getLong(UserMetaDataTable.lastSeenTime)
        }
    }
}