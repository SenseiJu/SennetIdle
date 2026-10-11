package gg.idlemob.idlemobcore.economy

import gg.idlemob.idlemobcore.economy.EconomyRepository.balance
import gg.idlemob.idlemobcore.economy.EconomyRepository.change
import gg.idlemob.idlemobcore.economy.EconomyRepository.set as setBalance
import gg.idlemob.idlemobcore.economy.EconomyRepository.top
import gg.idlemob.idlemobcore.framework.database.Database
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.user.UserManager
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import java.util.*

class BalanceChange(val uuid: UUID, val currency: Currency, val delta: Double, val balance: Double, val reason: String)

class EconomyService(private val database: Database, private val users: UserManager) {

    suspend fun balance(uuid: UUID, currency: Currency): Double? {
        users[uuid]?.let { return it.getBalance(currency) }
        return database.query { balance(uuid, currency) }
    }

    suspend fun deposit(uuid: UUID, currency: Currency, amount: Double, reason: String): Double {
        require(amount > 0) { "amount must be above 0" }
        return record(database.query { changeIn(this, uuid, currency, amount, reason) })
    }

    suspend fun withdraw(uuid: UUID, currency: Currency, amount: Double, reason: String): Double {
        require(amount > 0) { "amount must be above 0" }
        return record(database.query { changeIn(this, uuid, currency, -amount, reason) })
    }

    suspend fun set(uuid: UUID, currency: Currency, amount: Double, reason: String): Double {
        val change = database.query {
            val before = balance(uuid, currency) ?: throw UnknownAccountException(uuid)
            val after = setBalance(uuid, currency, amount, reason)
            BalanceChange(uuid, currency, after - before, after, reason)
        }
        return record(change)
    }

    suspend fun transfer(from: UUID, to: UUID, currency: Currency, amount: Double, reason: String) {
        require(amount > 0) { "amount must be above 0" }
        val (sent, received) = database.query {
            changeIn(this, from, currency, -amount, reason) to changeIn(this, to, currency, amount, reason)
        }
        record(sent)
        record(received)
    }

    suspend fun top(currency: Currency, limit: Int): List<Pair<UUID, Double>> = database.query { top(currency, limit) }

    fun changeIn(transaction: JdbcTransaction, uuid: UUID, currency: Currency, delta: Double, reason: String): BalanceChange {
        val balance = transaction.change(uuid, currency, delta, reason)
        return BalanceChange(uuid, currency, currency.round(delta), balance, reason)
    }

    fun record(change: BalanceChange): Double {
        users[change.uuid]?.cacheBalance(change.currency, change.balance)
        EventLogger.info("balance_changed") {
            "player_id" to change.uuid
            "currency" to change.currency.id
            "amount" to change.delta
            "balance" to change.balance
            "reason" to change.reason
        }
        return change.balance
    }
}
