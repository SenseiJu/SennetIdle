package gg.idlemob.idlemobcore.box

import gg.idlemob.idlemobcore.framework.extensions.giveOrDrop
import gg.idlemob.idlemobcore.IdleMobCore
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class BoxManager(
    private val plugin: IdleMobCore,
    private val boxes: BoxRegistry,
) : Listener {
    val items = BoxItems(plugin, boxes)

    private val messages
        get() = plugin.messages.boxes

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun give(player: Player, id: String, amount: Int) {
        val maxStack = boxes[id]?.display?.material?.maxStackSize ?: throw IllegalArgumentException("Unknown box $id")
        val stacks = (0 until amount step maxStack).map { items.create(id, minOf(maxStack, amount - it)) }
        player.giveOrDrop(stacks)
    }

    fun merge(player: Player) {
        val inventory = player.inventory
        val counts = LinkedHashMap<String, Int>()

        for (slot in 0 until inventory.storageContents.size) {
            val id = items.boxId(inventory.getItem(slot))?.takeIf { it in boxes } ?: continue
            counts.merge(id, inventory.getItem(slot)!!.amount, Int::plus)
            inventory.setItem(slot, null)
        }

        val made = LinkedHashMap<String, Int>()
        var changed = true
        while (changed) {
            changed = false
            counts.toList().forEach { (id, count) ->
                val merge = boxes[id]?.merge ?: return@forEach
                val upgrades = count / merge.amount
                if (upgrades == 0) {
                    return@forEach
                }

                counts[id] = count % merge.amount
                counts.merge(merge.into, upgrades, Int::plus)
                made.merge(merge.into, upgrades, Int::plus)
                changed = true
            }
        }

        counts.filterValues { it > 0 }.forEach { (id, count) -> give(player, id, count) }

        if (made.isEmpty()) {
            player.sendMessage(messages.nothingToMerge())
            return
        }

        player.sendMessage(messages.merged())
        made.forEach { (id, count) -> player.sendMessage(messages.mergedLine(count, boxes[id]!!.display.name)) }
        EventLogger.info("boxes_merged", player) { "made" to made.map { "${it.value}x ${it.key}" } }
    }

    private fun open(player: Player, item: ItemStack, all: Boolean) {
        val id = items.boxId(item) ?: return
        val box = boxes[id]
        if (box == null) {
            player.sendMessage(messages.unknownBox(id))
            return
        }

        val amount = if (all) item.amount else 1
        item.amount -= amount

        val received = LinkedHashMap<String, Int>()
        val console = plugin.server.consoleSender

        repeat(amount) {
            val rewards = box.guaranteedRewards.map { it.name to it.commands } +
                listOfNotNull(pickRandom(box.randomRewards)).map { it.name to it.commands }

            rewards.forEach { (name, commands) ->
                commands.forEach { plugin.server.dispatchCommand(console, it.replace("{player}", player.name)) }
                received.merge(name, 1, Int::plus)
            }
        }

        player.sendMessage(messages.opened(amount, box.display.name))
        received.forEach { (name, count) -> player.sendMessage(messages.rewardLine(count, name)) }

        EventLogger.info("box_opened", player) {
            "box" to id
            "amount" to amount
            "rewards" to received.map { "${it.value}x ${it.key}" }
        }
    }

    private fun pickRandom(rewards: List<RandomBoxReward>): RandomBoxReward? {
        if (rewards.isEmpty()) {
            return null
        }

        var roll = Random.nextDouble(rewards.sumOf { it.weight })
        for (reward in rewards) {
            roll -= reward.weight
            if (roll < 0) {
                return reward
            }
        }
        return rewards.last()
    }

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND || (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK)) {
            return
        }

        val item = event.item ?: return
        if (items.boxId(item) == null) {
            return
        }

        event.isCancelled = true
        open(event.player, event.player.inventory.itemInMainHand, event.player.isSneaking)
    }

    @EventHandler
    private fun onBlockPlace(event: BlockPlaceEvent) {
        if (items.boxId(event.itemInHand) != null) {
            event.isCancelled = true
        }
    }
}
