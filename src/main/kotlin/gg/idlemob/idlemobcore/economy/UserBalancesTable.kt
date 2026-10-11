package gg.idlemob.idlemobcore.economy

import gg.idlemob.idlemobcore.user.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object UserBalancesTable : Table("user_balances") {
    val userUuid = char("user_uuid", 36).references(UsersTable.uuid, onDelete = ReferenceOption.CASCADE)
    val currency = varchar("currency", 32)
    val amount = double("amount").default(0.0)

    override val primaryKey = PrimaryKey(userUuid, currency)
}
