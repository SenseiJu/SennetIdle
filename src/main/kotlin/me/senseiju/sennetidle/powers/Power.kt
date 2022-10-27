package me.senseiju.sennetidle.powers

import me.senseiju.sennetidle.powers.powerData.AdrenalineRush
import me.senseiju.sennetidle.powers.powerData.BasePower
import me.senseiju.sennetidle.powers.powerData.Fireball

enum class Power(val data: BasePower) {
    FIREBALL(Fireball),
    ADRENALINE_RUSH(AdrenalineRush)
}