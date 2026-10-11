package me.senseiju.sennetidle.powers.powerData

import me.senseiju.sennetidle.Message
import me.senseiju.sennetidle.powers.Power
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.utils.ModelItem
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

interface BasePower : ModelItem {
    val promotionUnlock: Int
    val description: String
    val cooldown: Long
    val slot: Int

    fun onRightClick(user: User)

    fun User.messageCooldown(power: Power) {
        message(
            Message.POWER_COOLDOWN_REMAINING,
            Placeholder.unparsed("power", name),
            Placeholder.unparsed("time_remaining", "${getCooldownRemaining(power) / 1000}")
        )
    }

    override fun itemStack(): ItemStack {
        return super.itemStack().apply {
            editMeta {
                it.persistentDataContainer.set(Power.namespacedKey, PersistentDataType.STRING, name)
            }
        }
    }
}
