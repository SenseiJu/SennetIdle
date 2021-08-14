package me.senseiju.sennetidle.database

import org.ktorm.schema.*

object UserReagentsTable : Table<Nothing>("user_reagents") {
    val userUUID = varchar("user_uuid").primaryKey()
    val reagentId = varchar("reagent_id").primaryKey()
    val reagentTotalAmountCrafted = long("reagent_level")
    val reagentAmount = long("reagent_amount")
}