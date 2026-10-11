package gg.idlemob.idlemobcore.prestige.menu

import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import gg.idlemob.idlemobcore.framework.menu.PageButton
import gg.idlemob.idlemobcore.framework.menu.button
import gg.idlemob.idlemobcore.framework.menu.dynamic
import gg.idlemob.idlemobcore.framework.menu.filler
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.plugin
import gg.idlemob.idlemobcore.prestige.PrestigeService
import gg.idlemob.idlemobcore.prestige.UpgradeConfig
import gg.idlemob.idlemobcore.prestige.UpgradeMode
import gg.idlemob.idlemobcore.prestige.UpgradeRegistry
import gg.idlemob.idlemobcore.stats.Stat
import gg.idlemob.idlemobcore.user.User
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer
import java.util.function.Function

class UpgradeMenu(private val upgrades: UpgradeRegistry, private val prestige: PrestigeService) {

    fun open(player: Player) {
        val user = plugin.users[player]

        val content = upgrades.entries.entries
            .sortedBy { it.value.sortOrder }
            .map { (id, upgrade) -> upgradeItem(player, user, id, upgrade) }

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "# # # < p > # # #",
            )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('#', filler())
            .addIngredient('<', PageButton(button(Material.ARROW, mini("<yellow>Previous page")), filler(), -1))
            .addIngredient('>', PageButton(button(Material.ARROW, mini("<yellow>Next page")), filler(), 1))
            .addIngredient('p', dynamic {
                button(Material.NETHER_STAR, mini("<gold><b><points> prestige points", Placeholder.unparsed("points", user.prestigePoints.toString())))
                    .setLore(listOf(mini("<gray>Earn more by prestiging.")))
            })
            .setContent(content)
            .build()

        Window.builder()
            .setUpperGui(gui)
            .setTitle(mini("<dark_gray>Upgrades"))
            .open(player)
    }

    private fun upgradeItem(player: Player, user: User, id: String, upgrade: UpgradeConfig): Item {
        return Item.builder()
            .setItemProvider(Function<Player, ItemProvider> {
                val level = user.getUpgrade(id)
                val maxed = level >= upgrade.maxLevel
                val cost = upgrade.costFor(level)

                val lore = mutableListOf(
                    mini("<gray>Level: <white><level></white>/<white><max>", Placeholder.unparsed("level", level.toString()), Placeholder.unparsed("max", upgrade.maxLevel.toString())),
                    mini(progressBar(level.toDouble() / upgrade.maxLevel)),
                    Component.empty(),
                    mini("<gray>Per level: <white><effect>", Placeholder.unparsed("effect", describe(upgrade, upgrade.perLevel))),
                    mini("<gray>Current: <white><effect>", Placeholder.unparsed("effect", describe(upgrade, upgrade.perLevel * level))),
                    Component.empty(),
                )
                lore += when {
                    maxed -> mini("<green>Maxed")
                    user.prestigePoints >= cost -> mini("<yellow>Click to upgrade for <white><cost></white> point(s)", Placeholder.unparsed("cost", cost.toString()))
                    else -> mini("<red>Costs <white><cost></white> point(s)", Placeholder.unparsed("cost", cost.toString()))
                }

                ItemBuilder(upgrade.display.toItemStack()).setName(upgrade.display.displayName()).setLore(lore).setGlint(maxed)
            })
            .updatePeriodically(20)
            .addClickHandler(Consumer<Click> {
                prestige.buyUpgrade(player, user, id)
            })
            .build()
    }

    private fun describe(upgrade: UpgradeConfig, value: Double): String {
        val amount = value.formatSuffixed()
        val percent = upgrade.mode == UpgradeMode.PERCENT || upgrade.stat in PERCENT_STATS
        val label = when (upgrade.stat) {
            Stat.DPS -> "DPS"
            Stat.CLICK_DAMAGE_PERCENT -> "click damage"
            Stat.CPS_LIMIT -> "CPS cap"
            Stat.DROP_CHANCE -> "drop chance"
            Stat.DROP_AMOUNT -> "drop amount"
            Stat.BOSS_TIME_LIMIT -> if (percent) "boss time" else "seconds of boss time"
            Stat.CRIT_CHANCE -> "critical hit chance"
            Stat.INSTANT_KILL_CHANCE -> "instant kill chance"
            Stat.BOX_DROP_CHANCE -> "box drop chance"
        }
        return "+$amount${if (percent) "%" else ""} $label"
    }

    private companion object {
        val PERCENT_STATS = setOf(Stat.CLICK_DAMAGE_PERCENT, Stat.CRIT_CHANCE, Stat.INSTANT_KILL_CHANCE)
    }
}
