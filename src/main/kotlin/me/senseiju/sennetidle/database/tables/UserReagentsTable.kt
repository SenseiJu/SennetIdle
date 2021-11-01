package me.senseiju.sennetidle.database.tables

object UserReagentsTable : Table("user_reagents") {
    val userUUID = "user_uuid"
    val reagentId = "reagent_id"

    val reagentTotalAmountCrafted = "reagent_total_amount_crafted"
    val reagentAmount = "reagent_amount"

    val UPDATE_USER_REAGENTS_QUERY = "INSERT INTO " +
            "`${tableName}`(`$userUUID`, `$reagentId`, `$reagentTotalAmountCrafted`, `$reagentAmount`)" +
            "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `$reagentTotalAmountCrafted`=?, `$reagentAmount`=?;"
    val SELECT_USER_REAGENTS_QUERY = selectAllQuery(userUUID)
}