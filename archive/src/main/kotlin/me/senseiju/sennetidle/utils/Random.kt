package me.senseiju.sennetidle.utils

import java.util.*

private val random = Random()

fun randomChance(chance: Float): Boolean {
    return random.nextFloat(0F, 100F) <= chance
}