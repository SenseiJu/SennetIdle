package gg.idlemob.idlemobcore.generator

import java.util.*

class GeneratorState(
    val slot: Int,
    val blueprint: String,
    val blueprintUuid: UUID,
    var uses: Int,
    var progressMs: Long = 0,
) {
    fun copy() = GeneratorState(slot, blueprint, blueprintUuid, uses, progressMs)
}
