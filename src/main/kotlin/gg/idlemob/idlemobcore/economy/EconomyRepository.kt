package gg.idlemob.idlemobcore.economy

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

class InsufficientFundsException(val currency: Currency) : RuntimeException("Not enough ${currency.id}")

class UnknownAccountException(val uuid: UUID) : RuntimeException("No account for $uuid")

object EconomyRepository {

    fun JdbcTransaction.balance(uuid: UUID, currency: Currency): Double? {
        return UserBalancesTable.selectAll()
            .where { (UserBalancesTable.userUuid eq uuid.toString()) and (UserBalancesTable.currency eq currency.id) }
            .singleOrNull()
            ?.get(UserBalancesTable.amount)
    }

    fun JdbcTransaction.change(uuid: UUID, currency: Currency, delta: Double, reason: String): Double {
        val current = lockedBalance(uuid, currency)
        val next = currency.round(current + delta)
        if (next < 0) {
            throw InsufficientFundsException(currency)
        }

        write(uuid, currency, next, next - current, reason)
        return next
    }

    fun JdbcTransaction.set(uuid: UUID, currency: Currency, amount: Double, reason: String): Double {
        val current = lockedBalance(uuid, currency)
        val next = currency.round(amount.coerceAtLeast(0.0))

        write(uuid, currency, next, next - current, reason)
        return next
    }

    fun JdbcTransaction.top(currency: Currency, limit: Int): List<Pair<UUID, Double>> {
        return UserBalancesTable.selectAll()
            .where { UserBalancesTable.currency eq currency.id }
            .orderBy(UserBalancesTable.amount, SortOrder.DESC)
            .limit(limit)
            .map { UUID.fromString(it[UserBalancesTable.userUuid]) to it[UserBalancesTable.amount] }
    }

    private fun JdbcTransaction.lockedBalance(uuid: UUID, currency: Currency): Double {
        return UserBalancesTable.selectAll()
            .where { (UserBalancesTable.userUuid eq uuid.toString()) and (UserBalancesTable.currency eq currency.id) }
            .forUpdate()
            .singleOrNull()
            ?.get(UserBalancesTable.amount)
            ?: throw UnknownAccountException(uuid)
    }

    private fun JdbcTransaction.write(uuid: UUID, currency: Currency, balance: Double, delta: Double, reason: String) {
        val id = uuid.toString()

        UserBalancesTable.update({ (UserBalancesTable.userUuid eq id) and (UserBalancesTable.currency eq currency.id) }) {
            it[amount] = balance
        }

        BalanceTransactionsTable.insert {
            it[userUuid] = id
            it[BalanceTransactionsTable.currency] = currency.id
            it[amount] = delta
            it[BalanceTransactionsTable.balance] = balance
            it[BalanceTransactionsTable.reason] = reason.take(128)
            it[createdAt] = System.currentTimeMillis()
        }
    }
}
