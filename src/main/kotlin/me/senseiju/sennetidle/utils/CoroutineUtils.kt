package me.senseiju.sennetidle.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun defaultScope(block: suspend () -> Unit) = CoroutineScope(Dispatchers.Default).launch { block() }
fun ioScope(block: suspend () -> Unit) = CoroutineScope(Dispatchers.IO).launch { block() }
