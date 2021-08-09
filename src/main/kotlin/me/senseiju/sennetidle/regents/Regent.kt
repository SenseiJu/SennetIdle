package me.senseiju.sennetidle.regents

import kotlinx.serialization.Serializable

@Serializable
class Regent(
    private val id: String,
    private val displayName: String,
    private val space: Float,
    private val value: Double,
    private val craftingResources: List<CraftingResource>
)

@Serializable
class CraftingResource(
    private val id: String,
    private val amount: Int
)