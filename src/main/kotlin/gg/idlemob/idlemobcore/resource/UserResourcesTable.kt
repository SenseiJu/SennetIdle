package gg.idlemob.idlemobcore.resource

import gg.idlemob.idlemobcore.user.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object UserResourcesTable : Table("user_resources") {
    val userUuid = char("user_uuid", 36).references(UsersTable.uuid, onDelete = ReferenceOption.CASCADE)
    val resource = varchar("resource", 64)
    val amount = double("amount").default(0.0)

    override val primaryKey = PrimaryKey(userUuid, resource)
}
