package me.senseiju.sennetidle.database.tables

object UserIdleMobTable : Table("user_idle_mob") {
    val userUUID = "user_uuid"

    val currentWave = "current_wave"

    val UPDATE_USER_IDLE_MOB_QUERY = "INSERT INTO " +
            "`$tableName`(`$userUUID`, `$currentWave`)" +
            "VALUES(?, ?) ON DUPLICATE KEY UPDATE `$currentWave`=?;"
    val SELECT_USER_IDLE_MOB_QUERY = selectAllQuery(userUUID)
}