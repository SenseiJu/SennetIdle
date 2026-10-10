package me.senseiju.sennetidle.upgrades

import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.EMPTY_COMPONENT
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sennetidle.utils.guiItem
import me.senseiju.sennetidle.utils.openGui
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder

private val userService = serviceProvider.get<UserService>()

private val TITLE_COMPONENT = "<dark_grey><b>Upgrades".component()

private val PLAYER_UPGRADE_COMPONENT = "<light_purple><b>Player Upgrades".component()
private val IDLE_MOB_UPGRADE_COMPONENT = "<light_purple><b>Idle Mob Upgrades".component()

fun Player.openUpgradeGui() {
    val user = userService.getUser(uniqueId)

    val gui = Gui.empty(9, 3)
    gui.setItem(3, 1, guiItem(ItemBuilder(Material.ARMOR_STAND).setName(PLAYER_UPGRADE_COMPONENT)) {
        openUpgradeListGui(PLAYER_TITLE_COMPONENT, user, Upgrade.playerUpgrades)
    })
    gui.setItem(5, 1, guiItem(ItemBuilder(Material.ZOMBIE_HEAD).setName(IDLE_MOB_UPGRADE_COMPONENT)) {
        openUpgradeListGui(IDLE_MOB_TITLE_COMPONENT, user, Upgrade.idleMobUpgrades)
    })

    openGui(TITLE_COMPONENT, gui)
}

private val IDLE_MOB_TITLE_COMPONENT = "<dark_grey><b>Idle Mob Upgrades".component()
private val PLAYER_TITLE_COMPONENT = "<dark_grey><b>Player Upgrades".component()

private fun Player.openUpgradeListGui(title: Component, user: User, upgrades: Collection<Upgrade>) {
    val gui = Gui.empty(9, 3)
    gui.addItems(*upgrades.map { createUpgradeItem(user, it) }.toTypedArray())

    openGui(title, gui)
}

private fun createUpgradeItem(user: User, upgrade: Upgrade): Item {
    return guiItem(
        provider = { ItemBuilder(upgrade.data.itemStack()).setLore(createLore(upgrade, user.getUpgradeLevel(upgrade))) },
        onClick = { click ->
            if (click.clickType == ClickType.LEFT && user.canLevelUpgrade(upgrade)) {
                user.addUpgradeLevel(upgrade, 1)
            }
        }
    )
}

private fun createLore(upgrade: Upgrade, level: Int): List<Component> {
    val lore = arrayListOf(EMPTY_COMPONENT)

    lore.add("<grey>${upgrade.data.description}".component())
    lore.add(EMPTY_COMPONENT)
    lore.add("<aqua>Cost: <yellow>1 Upgrade Point".component())
    lore.add("<aqua>Current level: <yellow>$level/${upgrade.data.maxLevel}".component())

    return lore
}
