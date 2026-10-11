package gg.idlemob.idlemobcore.user

import gg.idlemob.idlemobcore.economy.Currency
import gg.idlemob.idlemobcore.economy.UserBalancesTable
import gg.idlemob.idlemobcore.framework.database.Database
import gg.idlemob.idlemobcore.generator.GeneratorState
import gg.idlemob.idlemobcore.generator.UserGeneratorsTable
import gg.idlemob.idlemobcore.prestige.UserPrestigeProgressTable
import gg.idlemob.idlemobcore.prestige.UserUpgradesTable
import gg.idlemob.idlemobcore.resource.UserResourcesTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

class UserRepository(private val database: Database) {

    suspend fun loadOrCreate(uuid: UUID): User = database.query {
        load(uuid) ?: create(uuid)
    }

    suspend fun save(snapshots: Collection<UserSnapshot>) = database.query {
        write(snapshots)
    }

    fun saveBlocking(snapshots: Collection<UserSnapshot>) = database.transaction {
        write(snapshots)
    }

    fun JdbcTransaction.write(snapshots: Collection<UserSnapshot>) {
        val now = System.currentTimeMillis()

        snapshots.forEach { snapshot ->
            val id = snapshot.uuid.toString()

            UsersTable.update({ UsersTable.uuid eq id }) {
                it[stage] = snapshot.stage
                it[wave] = snapshot.wave
                it[prestige] = snapshot.prestige
                it[prestigePoints] = snapshot.prestigePoints
                it[offlineTimeMs] = snapshot.offlineTimeMs
                it[lastSeen] = now
            }

            UserPrestigeProgressTable.deleteWhere { userUuid eq id }
            UserPrestigeProgressTable.batchInsert(snapshot.prestigeProgress.entries) { (resource, amount) ->
                this[UserPrestigeProgressTable.userUuid] = id
                this[UserPrestigeProgressTable.resource] = resource
                this[UserPrestigeProgressTable.amount] = amount
            }

            snapshot.generators.forEach { state ->
                UserGeneratorsTable.update({
                    (UserGeneratorsTable.userUuid eq id) and (UserGeneratorsTable.slot eq state.slot) and
                        (UserGeneratorsTable.blueprintUuid eq state.blueprintUuid.toString())
                }) {
                    it[uses] = state.uses
                    it[progressMs] = state.progressMs
                }
            }
        }

        val resources = snapshots.flatMap { snapshot -> snapshot.resources.map { Triple(snapshot.uuid.toString(), it.key, it.value) } }
        if (resources.isNotEmpty()) {
            UserResourcesTable.batchUpsert(resources) { (uuid, resource, amount) ->
                this[UserResourcesTable.userUuid] = uuid
                this[UserResourcesTable.resource] = resource
                this[UserResourcesTable.amount] = amount
            }
        }

        val upgrades = snapshots.flatMap { snapshot -> snapshot.upgrades.map { Triple(snapshot.uuid.toString(), it.key, it.value) } }
        if (upgrades.isNotEmpty()) {
            UserUpgradesTable.batchUpsert(upgrades) { (uuid, upgrade, level) ->
                this[UserUpgradesTable.userUuid] = uuid
                this[UserUpgradesTable.upgrade] = upgrade
                this[UserUpgradesTable.level] = level
            }
        }
    }

    private fun JdbcTransaction.load(uuid: UUID): User? {
        val id = uuid.toString()
        val row = UsersTable.selectAll().where { UsersTable.uuid eq id }.singleOrNull() ?: return null

        insertStartingBalances(id)

        val resources = UserResourcesTable.selectAll()
            .where { UserResourcesTable.userUuid eq id }
            .associate { it[UserResourcesTable.resource] to it[UserResourcesTable.amount] }

        val balances = UserBalancesTable.selectAll()
            .where { UserBalancesTable.userUuid eq id }
            .mapNotNull { row -> Currency.byId(row[UserBalancesTable.currency])?.let { it to row[UserBalancesTable.amount] } }
            .toMap()

        val prestigeProgress = UserPrestigeProgressTable.selectAll()
            .where { UserPrestigeProgressTable.userUuid eq id }
            .associate { it[UserPrestigeProgressTable.resource] to it[UserPrestigeProgressTable.amount] }

        val upgrades = UserUpgradesTable.selectAll()
            .where { UserUpgradesTable.userUuid eq id }
            .associate { it[UserUpgradesTable.upgrade] to it[UserUpgradesTable.level] }

        val generators = UserGeneratorsTable.selectAll()
            .where { UserGeneratorsTable.userUuid eq id }
            .map {
                GeneratorState(
                    slot = it[UserGeneratorsTable.slot],
                    blueprint = it[UserGeneratorsTable.blueprint],
                    blueprintUuid = UUID.fromString(it[UserGeneratorsTable.blueprintUuid]),
                    uses = it[UserGeneratorsTable.uses],
                    progressMs = it[UserGeneratorsTable.progressMs],
                )
            }

        return User(
            uuid = uuid,
            createdAt = row[UsersTable.createdAt],
            stage = row[UsersTable.stage],
            wave = row[UsersTable.wave],
            prestige = row[UsersTable.prestige],
            prestigePoints = row[UsersTable.prestigePoints],
            offlineTimeMs = row[UsersTable.offlineTimeMs],
            resources = resources,
            balances = balances,
            prestigeProgress = prestigeProgress,
            upgrades = upgrades,
            generators = generators,
        )
    }

    private fun JdbcTransaction.create(uuid: UUID): User {
        val now = System.currentTimeMillis()
        val id = uuid.toString()

        UsersTable.insert {
            it[UsersTable.uuid] = id
            it[lastSeen] = now
            it[createdAt] = now
        }
        insertStartingBalances(id)

        return User(uuid, createdAt = now, balances = Currency.entries.associateWith { it.startingBalance })
    }

    private fun JdbcTransaction.insertStartingBalances(id: String) {
        Currency.entries.forEach { currency ->
            UserBalancesTable.insertIgnore {
                it[userUuid] = id
                it[UserBalancesTable.currency] = currency.id
                it[amount] = currency.startingBalance
            }
        }
    }
}
