package me.senseiju.sennetidle.utils.extensions

import javax.sql.rowset.CachedRowSet

fun CachedRowSet.forEach(block: () -> Unit) {
    while (next()) {
        block()
    }
}