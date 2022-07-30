package me.senseiju.sennetidle.utils.extensions

import me.senseiju.sentils.storage.Database
import me.senseiju.sentils.storage.Replacements

fun Database.updateBatchQuery(q: String, replacements: List<Replacements>) {
    updateBatchQuery(q, *replacements.toTypedArray())
}