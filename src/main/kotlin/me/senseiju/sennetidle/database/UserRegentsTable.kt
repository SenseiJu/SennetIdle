package me.senseiju.sennetidle.database

import org.ktorm.schema.*

object UserRegentsTable : Table<Nothing>("user_regents") {
    val userUUID = uuid("user_uuid").primaryKey()
    val regentId = varchar("regent_id").primaryKey()
    val regentLevel = int("regent_level")
    val regentAmount = long("regent_amount")
}