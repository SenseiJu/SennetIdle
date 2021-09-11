package me.senseiju.sennetidle.database

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object UserMetaDataTable : Table<Nothing>("user_meta_data") {
    val userUUID = varchar("user_uuid").primaryKey()
    val lastSeenTime = long("last_seen_time")
}