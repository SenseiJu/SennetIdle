package me.senseiju.sennetidle.database.tables

object UserActiveGeneratorsTable : Table("user_active_generators") {
    val userUUID = "user_uuid"
    val generatorId = "generator_id"

    val reagentId = "reagent_id"

    val UPDATE_USER_CRAFTING_STATIONS = "INSERT INTO " +
            "`$tableName`(`${userUUID}`, `${generatorId}`, `${reagentId}`)" +
            "VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `${reagentId}`=?;"
    val SELECT_USER_CRAFTING_STATIONS = selectAllQuery(userUUID)
}