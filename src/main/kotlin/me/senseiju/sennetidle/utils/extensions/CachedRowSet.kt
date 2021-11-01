package me.senseiju.sennetidle.utils.extensions

import javax.sql.rowset.CachedRowSet

fun CachedRowSet.forEach(block: (CachedRowSet) -> Unit) {
    while (next()) {
        block(this)
    }
}