package gg.idlemob.idlemobcore.prestige.menu

import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import gg.idlemob.idlemobcore.framework.menu.button
import gg.idlemob.idlemobcore.framework.menu.dynamic
import gg.idlemob.idlemobcore.framework.menu.filler
import gg.idlemob.idlemobcore.framework.text.mini
import gg.idlemob.idlemobcore.plugin
import gg.idlemob.idlemobcore.prestige.PrestigeLevelConfig
import gg.idlemob.idlemobcore.prestige.PrestigeService
import gg.idlemob.idlemobcore.user.User
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer
import java.util.function.Function

class PrestigeMenu(private val prestige: PrestigeService, private val upgradeMenu: UpgradeMenu) {

    fun open(player: Player) {
        val user = plugin.users[player]
        val level = prestige.nextLevel(user)

        lateinit var gui: Gui
        gui = Gui.builder()
            .setStructure(
                "# # # # i # # # #",
                "# . . . . . . . #",
                "# # # # # # # # #",
                "# # s # c # u # #",
                "# # # # p # # # #",
                "# # # # # # # # #",
            )
            .addIngredient('#', filler())
            .addIngredient('i', dynamic { infoItem(user, level) })
            .addIngredient('s', dynamic { stageItem(user, level) })
            .addIngredient('c', dynamic { costItem(user, level) })
            .addIngredient('u', clickable(button(Material.ENCHANTED_BOOK, mini("<light_purple><b>Upgrades")).setLore(listOf(
                mini("<gray>Spend prestige points on"),
                mini("<gray>permanent upgrades."),
                Component.empty(),
                mini("<yellow>Click to open"),
            ))) { upgradeMenu.open(player) })
            .addIngredient('p', prestigeButton(player, user, level))
            .build()

        level?.resources?.keys?.take(7)?.forEachIndexed { index, resource ->
            gui.setItem(index + 1, 1, resourceItem(player, user, resource) { gui.notifyWindows() })
        }

        Window.builder()
            .setUpperGui(gui)
            .setTitle(mini("<dark_gray>Prestige"))
            .open(player)
    }

    private fun infoItem(user: User, level: PrestigeLevelConfig?): ItemProvider {
        if (level == null) {
            return button(Material.NETHER_STAR, mini("<gold><b>Prestige <prestige>", Placeholder.unparsed("prestige", user.prestige.toString())))
                .setLore(listOf(mini("<gray>You've reached the highest prestige.")))
        }

        return button(Material.NETHER_STAR, mini("<gold><b>Prestige <current> to <next>", Placeholder.unparsed("current", user.prestige.toString()), Placeholder.unparsed("next", (user.prestige + 1).toString())))
            .setLore(listOf(
                mini("<gray>Resets your stage and wave, but keeps"),
                mini("<gray>everything you haven't handed in."),
                Component.empty(),
                mini("<gray>Rewards: <white><points> prestige points", Placeholder.unparsed("points", level.points.toString())),
                mini("<gray>Your points: <white><points>", Placeholder.unparsed("points", user.prestigePoints.toString())),
            ))
    }

    private fun stageItem(user: User, level: PrestigeLevelConfig?): ItemProvider {
        if (level == null) {
            return filler()
        }

        val done = user.stage >= level.requiredStage
        return button(if (done) Material.LIME_DYE else Material.GRAY_DYE, mini("<aqua><b>Reach stage <stage>", Placeholder.unparsed("stage", level.requiredStage.toString())))
            .setLore(listOf(mark(done, "Current stage: <white>${user.stage}")))
    }

    private fun costItem(user: User, level: PrestigeLevelConfig?): ItemProvider {
        val costs = level?.costs() ?: return filler()
        if (costs.isEmpty()) {
            return button(Material.LIME_DYE, mini("<green><b>Free")).setLore(listOf(mini("<gray>This prestige has no currency cost.")))
        }

        val lines = costs.map { (currency, amount) ->
            mark(user.getBalance(currency) >= amount, "${currency.formatColored(amount)} <gray>(you have ${currency.formatColored(user.getBalance(currency))}<gray>)")
        }
        return button(Material.GOLD_INGOT, mini("<yellow><b>Cost")).setLore(lines)
    }

    private fun resourceItem(player: Player, user: User, resource: String, refresh: () -> Unit): Item {
        val config = plugin.resources[resource]

        return Item.builder()
            .setItemProvider(Function<Player, ItemProvider> {
                val level = prestige.nextLevel(user) ?: return@Function filler()
                val required = level.resources[resource] ?: 0.0
                val progress = user.getPrestigeProgress(resource)
                val remaining = prestige.remaining(user, level, resource)
                val handIn = minOf(user.getResource(resource), remaining)

                val builder = config?.let { ItemBuilder(it.display.toItemStack()).setName(it.display.displayName()) }
                    ?: button(Material.BARRIER, Component.text(resource))

                val lore = mutableListOf(
                    mini("<gray>Handed in: <white><progress></white> / <white><required>", Placeholder.unparsed("progress", progress.formatSuffixed()), Placeholder.unparsed("required", required.formatSuffixed())),
                    mini(progressBar(progress / required)),
                    mini("<gray>You have: <white><have>", Placeholder.unparsed("have", user.getResource(resource).formatSuffixed())),
                    Component.empty(),
                )
                lore += when {
                    remaining <= 0 -> mini("<green>Complete")
                    handIn > 0 -> mini("<yellow>Click to hand in <amount>", Placeholder.unparsed("amount", handIn.formatSuffixed()))
                    else -> mini("<red>You don't have any to hand in")
                }

                builder.setLore(lore).setGlint(remaining <= 0)
            })
            .updatePeriodically(20)
            .addClickHandler(Consumer<Click> {
                prestige.commit(player, user, resource)
                refresh()
            })
            .build()
    }

    private fun prestigeButton(player: Player, user: User, level: PrestigeLevelConfig?): Item {
        return Item.builder()
            .setItemProvider(Function<Player, ItemProvider> {
                when {
                    level == null -> filler()
                    prestige.isReady(user, level) -> button(Material.LIME_STAINED_GLASS_PANE, mini("<green><b>PRESTIGE")).setLore(listOf(mini("<yellow>Click to prestige")))
                    else -> button(Material.RED_STAINED_GLASS_PANE, mini("<red><b>PRESTIGE")).setLore(listOf(mini("<gray>Complete every requirement first.")))
                }
            })
            .updatePeriodically(20)
            .addClickHandler(Consumer<Click> {
                if (level != null) {
                    player.closeInventory()
                    prestige.prestige(player, user)
                }
            })
            .build()
    }

    private fun clickable(provider: ItemProvider, onClick: () -> Unit): Item {
        return Item.builder()
            .setItemProvider(provider)
            .addClickHandler(Consumer<Click> { onClick() })
            .build()
    }

    private fun mark(done: Boolean, text: String): Component = mini(if (done) "<green>$text" else "<red>$text")
}

fun progressBar(fraction: Double, length: Int = 20): String {
    val filled = (fraction.coerceIn(0.0, 1.0) * length).toInt()
    return "<green>${"|".repeat(filled)}<dark_gray>${"|".repeat(length - filled)} <gray>${(fraction.coerceIn(0.0, 1.0) * 100).toInt()}%"
}
