package me.senseiju.sennetidle.users

import dev.triumphteam.gui.builder.item.ItemBuilder
import org.bukkit.inventory.ItemStack
import java.util.*

class User(
    val uuid: UUID,
    val regents: HashMap<String, UserRegent>
)

class UserRegent(
    val id: String,
    var level: Int = 0,
    var amount: Long = 0
)