package me.senseiju.sennetidle.database.tables

abstract class Table(val tableName: String) {

    fun selectAllQuery(vararg conditionColumns: String) =
        "SELECT * FROM `$tableName` WHERE ${conditionColumns.joinToString(", ", "`", "`=?")};"
}