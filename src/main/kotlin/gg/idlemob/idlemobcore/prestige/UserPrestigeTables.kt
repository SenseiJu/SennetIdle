package gg.idlemob.idlemobcore.prestige

import gg.idlemob.idlemobcore.user.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object UserPrestigeProgressTable : Table("user_prestige_progress") {
    val userUuid = char("user_uuid", 36).references(UsersTable.uuid, onDelete = ReferenceOption.CASCADE)
    val resource = varchar("resource", 64)
    val amount = double("amount").default(0.0)

    override val primaryKey = PrimaryKey(userUuid, resource)
}

object UserUpgradesTable : Table("user_upgrades") {
    val userUuid = char("user_uuid", 36).references(UsersTable.uuid, onDelete = ReferenceOption.CASCADE)
    val upgrade = varchar("upgrade", 64)
    val level = integer("level").default(0)

    override val primaryKey = PrimaryKey(userUuid, upgrade)
}
