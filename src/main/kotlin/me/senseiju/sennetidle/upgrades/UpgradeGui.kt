package me.senseiju.sennetidle.upgrades

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.user.User
import me.senseiju.sennetidle.user.UserService
import me.senseiju.sennetidle.utils.EMPTY_COMPONENT
import me.senseiju.sennetidle.utils.defaultScope
import me.senseiju.sennetidle.utils.extensions.component
import me.senseiju.sennetidle.utils.openSync
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

private val userService = serviceProvider.get<UserService>()

private val TITLE_COMPONENT = "<dark_grey><b>Upgrades".component()

private val PLAYER_UPGRADE_COMPONENT = "<light_purple><b>Player Upgrades".component()
private val IDLE_MOB_UPGRADE_COMPONENT = "<light_purple><b>Idle Mob Upgrades".component()

fun Player.openUpgradeGui() {
    defaultScope {
        val gui = Gui.gui()
            .title(TITLE_COMPONENT)
            .rows(3)
            .disableAllInteractions()
            .create()

        val user = userService.getUser(this.uniqueId)

        val playerUpgradesItem = ItemBuilder.from(Material.ARMOR_STAND)
            .name(PLAYER_UPGRADE_COMPONENT)
            .asGuiItem {
                openPlayerUpgradeGui(user)
            }

        val idleMobUpgradesItem = ItemBuilder.from(Material.ZOMBIE_HEAD)
            .name(IDLE_MOB_UPGRADE_COMPONENT)
            .asGuiItem {
                openIdleMobUpgradeGui(user)
            }

        gui.setItem(2, 4, playerUpgradesItem)
        gui.setItem(2, 6, idleMobUpgradesItem)

        gui.openSync(this)
    }
}

private val IDLE_MOB_TITLE_COMPONENT = "<dark_grey><b>Idle Mob Upgrades".component()
private fun Player.openIdleMobUpgradeGui(user: User) {
    defaultScope {
        val gui = Gui.gui()
            .title(IDLE_MOB_TITLE_COMPONENT)
            .rows(3)
            .disableAllInteractions()
            .create()

        Upgrade.idleMobUpgrades.forEach { gui.addItem(createUpgradeItem(user, it)) }

        gui.openSync(this)
    }
}

private val PLAYER_TITLE_COMPONENT = "<dark_grey><b>Player Upgrades".component()
private fun Player.openPlayerUpgradeGui(user: User) {
    defaultScope {
        val gui = Gui.gui()
            .title(PLAYER_TITLE_COMPONENT)
            .rows(3)
            .disableAllInteractions()
            .create()

        Upgrade.playerUpgrades.forEach { gui.addItem(createUpgradeItem(user, it)) }

        gui.openSync(this)
    }
}

private fun createUpgradeItem(user: User, upgrade: Upgrade): GuiItem {
    return ItemBuilder.from(upgrade.data.itemStack())
        .lore(createLore(upgrade, user.getUpgradeLevel(upgrade)))
        .asGuiItem {
            if (it.click == ClickType.LEFT) {
                if (user.canLevelUpgrade(upgrade)) {
                    user.addUpgradeLevel(upgrade, 1)
                }
            }

            it.currentItem?.editMeta { meta ->
                meta.lore(createLore(upgrade, user.getUpgradeLevel(upgrade)))
            }
        }
}

private fun createLore(upgrade: Upgrade, level: Int): List<Component> {
    val lore = arrayListOf(EMPTY_COMPONENT)

    lore.add("<grey>${upgrade.data.description}".component())
    lore.add(EMPTY_COMPONENT)
    lore.add("<aqua>Cost: <yellow>1 Upgrade Point".component())
    lore.add("<aqua>Current level: <yellow>$level/${upgrade.data.maxLevel}".component())

    return lore
}