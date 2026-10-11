package gg.idlemob.idlemobcore.economy

import org.jetbrains.exposed.v1.core.Table

object BalanceTransactionsTable : Table("balance_transactions") {
    val id = long("id").autoIncrement()
    val userUuid = char("user_uuid", 36)
    val currency = varchar("currency", 32)
    val amount = double("amount")
    val balance = double("balance")
    val reason = varchar("reason", 128)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
