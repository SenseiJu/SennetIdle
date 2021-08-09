package me.senseiju.sennetidle.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.util.*

object PlayerRegentsTable : Table("player_regents") {
    val playerUUID: Column<UUID> = uuid("player_uuid")
    val regentId: Column<String> = varchar("regent_id", 32)
    val regentAmount: Column<Long> = long("regent_amount")
}