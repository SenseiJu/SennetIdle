package me.senseiju.sennetidle.database.tables

object UserMetaDataTable : Table("user_meta_data") {
    val userUUID = "user_uuid"

    val lastSeenTime = "last_seen_time"

    val UPDATE_USER_META_DATA = "INSERT INTO " +
            "`$tableName`(`${userUUID}`, `${lastSeenTime}`)" +
            "VALUES(?, ?) ON DUPLICATE KEY UPDATE `${lastSeenTime}`=?;"
    val SELECT_USER_META_DATA = selectAllQuery(userUUID)
}