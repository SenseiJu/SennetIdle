package gg.idlemob.idlemobcore.user

import org.jetbrains.exposed.v1.core.Table

object UsersTable : Table("users") {
    val uuid = char("uuid", 36)
    val stage = integer("stage").default(1)
    val wave = integer("wave").default(1)
    val prestige = integer("prestige").default(0)
    val prestigePoints = integer("prestige_points").default(0)
    val offlineTimeMs = long("offline_time_ms").default(0)
    val lastSeen = long("last_seen")
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(uuid)
}
