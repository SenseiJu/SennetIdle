package me.senseiju.sennetidle.database

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object UserIdleMobTable : Table<Nothing>("user_idle_mob") {
    val userUUID = varchar("user_uuid").primaryKey()
    val currentWave = int("current_wave")
}