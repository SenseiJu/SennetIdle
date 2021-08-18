package me.senseiju.sennetidle.database

import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object UserActiveGeneratorsTable : Table<Nothing>("user_active_generators") {
    val userUUID = varchar("user_uuid").primaryKey()
    val generatorId = varchar("generator_id").primaryKey()
    val reagentId = varchar("reagent_id")
}