package gg.idlemob.idlemobcore.generator

import gg.idlemob.idlemobcore.user.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object UserGeneratorsTable : Table("user_generators") {
    val userUuid = char("user_uuid", 36).references(UsersTable.uuid, onDelete = ReferenceOption.CASCADE)
    val slot = integer("slot")
    val blueprint = varchar("blueprint", 64)
    val blueprintUuid = char("blueprint_uuid", 36).uniqueIndex("uq_user_generators_blueprint")
    val uses = integer("uses")
    val progressMs = long("progress_ms").default(0)

    override val primaryKey = PrimaryKey(userUuid, slot)
}
