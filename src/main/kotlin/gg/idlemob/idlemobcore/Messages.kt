package gg.idlemob.idlemobcore

import gg.idlemob.idlemobcore.economy.Currency
import gg.idlemob.idlemobcore.framework.extensions.formatSuffixed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.PostProcess

private val miniMessage = MiniMessage.miniMessage()
private var prefixResolver: TagResolver = TagResolver.empty()

fun renderMessage(template: String, vararg resolvers: TagResolver): Component {
    return miniMessage.deserialize(template, prefixResolver, *resolvers)
}

@ConfigSerializable
class Messages {
    var prefix = "<#F97316><b>IdleMob</b> <dark_gray>»</dark_gray> "
    var admin = AdminMessages()
    var users = UserMessages()
    var fight = FightMessages()
    var resources = ResourceMessages()
    var boxes = BoxMessages()
    var economy = EconomyMessages()
    var prestige = PrestigeMessages()
    var generators = GeneratorMessages()

    @PostProcess
    private fun applyPrefix() {
        prefixResolver = Placeholder.parsed("prefix", prefix)
    }
}

@ConfigSerializable
class AdminMessages {
    private var reloadSuccess = "<prefix><green>Reloaded the config files, <resources> resources, <blueprints> blueprints and <boxes> boxes loaded. Database changes need a restart."
    private var reloadProblems = "<prefix><yellow><problems> content file(s) have problems and were skipped, check the console."
    private var reloadFailed = "<prefix><red>Could not reload: <error>. The old config is still in use."
    private var debugEnabled = "<prefix><gray>Debug logging is now <green>enabled</green>."
    private var debugDisabled = "<prefix><gray>Debug logging is now <red>disabled</red>."

    fun reloadSuccess(resources: Int, blueprints: Int, boxes: Int) = renderMessage(
        reloadSuccess,
        Placeholder.unparsed("boxes", boxes.toString()),
        Placeholder.unparsed("resources", resources.toString()),
        Placeholder.unparsed("blueprints", blueprints.toString()),
    )

    fun reloadProblems(problems: Int) = renderMessage(reloadProblems, Placeholder.unparsed("problems", problems.toString()))

    fun reloadFailed(error: String) = renderMessage(reloadFailed, Placeholder.unparsed("error", error))

    fun debugToggled(enabled: Boolean) = renderMessage(if (enabled) debugEnabled else debugDisabled)
}

@ConfigSerializable
class UserMessages {
    private var loadFailed = "<red>Couldn't load your data, please try joining again. If this keeps happening, contact staff."

    fun loadFailed() = renderMessage(loadFailed)
}

@ConfigSerializable
class FightMessages {
    private var bossSpawnedTitle = "<red><b>BOSS WAVE"
    private var bossSpawnedSubtitle = "<gray>Kill it before the time runs out!"
    private var bossFailed = "<prefix><red>You ran out of time! Back to the start of stage <stage>."
    private var stageCompleted = "<prefix><green>Stage <white><stage></white> complete!"

    fun bossSpawnedTitle() = renderMessage(bossSpawnedTitle)

    fun bossSpawnedSubtitle() = renderMessage(bossSpawnedSubtitle)

    fun bossFailed(stage: Int) = renderMessage(bossFailed, Placeholder.unparsed("stage", stage.toString()))

    fun stageCompleted(stage: Int) = renderMessage(stageCompleted, Placeholder.unparsed("stage", stage.toString()))
}

@ConfigSerializable
class ResourceMessages {
    private var crafted = "<prefix><green>Crafted <white><amount>x</white> <name><green>."
    private var notEnough = "<prefix><red>You don't have enough to craft that."
    private var given = "<prefix><green>Gave <white><amount>x</white> <name> <green>to <white><player></white>."
    private var unknownResource = "<prefix><red>There's no resource called <id>."
    private var invalidAmount = "<prefix><red>'<input>' isn't a valid amount."
    private var userNotLoaded = "<prefix><red><player>'s data isn't loaded."

    fun crafted(amount: Int, name: String) = renderMessage(
        crafted,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("name", name),
    )

    fun notEnough() = renderMessage(notEnough)

    fun given(amount: Double, name: String, player: String) = renderMessage(
        given,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("name", name),
        Placeholder.unparsed("player", player),
    )

    fun unknownResource(id: String) = renderMessage(unknownResource, Placeholder.unparsed("id", id))

    fun invalidAmount(input: String) = renderMessage(invalidAmount, Placeholder.unparsed("input", input))

    fun userNotLoaded(player: String) = renderMessage(userNotLoaded, Placeholder.unparsed("player", player))
}

@ConfigSerializable
class BoxMessages {
    private var opened = "<prefix><green>You opened <white><amount>x</white> <box><green>:"
    private var rewardLine = "<dark_gray> ➥ <white><amount>x</white> <reward>"
    private var merged = "<prefix><green>Merged your boxes into:"
    private var mergedLine = "<dark_gray> ➥ <white><amount>x</white> <box>"
    private var nothingToMerge = "<prefix><red>You don't have enough boxes to merge."
    private var given = "<prefix><green>Gave <white><amount>x</white> <box> <green>to <white><player></white>."
    private var received = "<prefix><green>You received <white><amount>x</white> <box><green>!"
    private var unknownBox = "<prefix><red>There's no box called <id>."
    private var invalidAmount = "<prefix><red>The amount must be between 1 and <max>."

    fun opened(amount: Int, box: String) = renderMessage(
        opened,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("box", box),
    )

    fun rewardLine(amount: Int, reward: String) = renderMessage(
        rewardLine,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("reward", reward),
    )

    fun merged() = renderMessage(merged)

    fun mergedLine(amount: Int, box: String) = renderMessage(
        mergedLine,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("box", box),
    )

    fun nothingToMerge() = renderMessage(nothingToMerge)

    fun given(amount: Int, box: String, player: String) = renderMessage(
        given,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("box", box),
        Placeholder.unparsed("player", player),
    )

    fun received(amount: Int, box: String) = renderMessage(
        received,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("box", box),
    )

    fun unknownBox(id: String) = renderMessage(unknownBox, Placeholder.unparsed("id", id))

    fun invalidAmount(max: Int) = renderMessage(invalidAmount, Placeholder.unparsed("max", max.toString()))
}

@ConfigSerializable
class EconomyMessages {
    private var balanceHeader = "<prefix><gray><player>'s balance:"
    private var balanceLine = "<dark_gray> ➥ <gray><currency>: <amount>"
    private var paid = "<prefix><green>You sent <amount> <green>to <white><player></white>."
    private var received = "<prefix><green>You received <amount> <green>from <white><player></white>."
    private var notEnough = "<prefix><red>You don't have enough <currency>."
    private var notPayable = "<prefix><red><currency> can't be sent to other players."
    private var invalidAmount = "<prefix><red>'<input>' isn't a valid amount."
    private var unknownPlayer = "<prefix><red><player> has never played here."
    private var paySelf = "<prefix><red>You can't pay yourself."
    private var topHeader = "<prefix><gold>Top <currency>:"
    private var topLine = "<dark_gray> <rank>. <white><player> <dark_gray>- <amount>"
    private var topEmpty = "<dark_gray> Nobody yet."
    private var adminUpdated = "<prefix><green><player>'s <currency> is now <amount><green>."
    private var unknownCurrency = "<prefix><red>There's no currency called <input>."
    private var failed = "<prefix><red>Something went wrong, please try again."

    fun balanceHeader(player: String) = renderMessage(balanceHeader, Placeholder.unparsed("player", player))

    fun balanceLine(currency: Currency, amount: Double) = renderMessage(
        balanceLine,
        Placeholder.unparsed("currency", currency.displayName),
        Placeholder.parsed("amount", currency.formatColored(amount)),
    )

    fun paid(currency: Currency, amount: Double, player: String) = renderMessage(
        paid,
        Placeholder.parsed("amount", currency.formatColored(amount)),
        Placeholder.unparsed("player", player),
    )

    fun received(currency: Currency, amount: Double, player: String) = renderMessage(
        received,
        Placeholder.parsed("amount", currency.formatColored(amount)),
        Placeholder.unparsed("player", player),
    )

    fun notEnough(currency: Currency) = renderMessage(notEnough, Placeholder.unparsed("currency", currency.displayName))

    fun notPayable(currency: Currency) = renderMessage(notPayable, Placeholder.unparsed("currency", currency.displayName))

    fun invalidAmount(input: String) = renderMessage(invalidAmount, Placeholder.unparsed("input", input))

    fun unknownPlayer(player: String) = renderMessage(unknownPlayer, Placeholder.unparsed("player", player))

    fun paySelf() = renderMessage(paySelf)

    fun topHeader(currency: Currency) = renderMessage(topHeader, Placeholder.unparsed("currency", currency.displayName))

    fun topLine(rank: Int, player: String, currency: Currency, amount: Double) = renderMessage(
        topLine,
        Placeholder.unparsed("rank", rank.toString()),
        Placeholder.unparsed("player", player),
        Placeholder.parsed("amount", currency.formatColored(amount)),
    )

    fun topEmpty() = renderMessage(topEmpty)

    fun adminUpdated(player: String, currency: Currency, amount: Double) = renderMessage(
        adminUpdated,
        Placeholder.unparsed("player", player),
        Placeholder.unparsed("currency", currency.displayName),
        Placeholder.parsed("amount", currency.formatColored(amount)),
    )

    fun unknownCurrency(input: String) = renderMessage(unknownCurrency, Placeholder.unparsed("input", input))

    fun failed() = renderMessage(failed)
}

@ConfigSerializable
class PrestigeMessages {
    private var committed = "<prefix><green>Handed in <white><amount>x</white> <resource><green>."
    private var nothingToCommit = "<prefix><red>You don't have any <resource> <red>to hand in."
    private var notReady = "<prefix><red>You haven't met all the requirements yet."
    private var maxPrestige = "<prefix><gray>You've reached the highest prestige."
    private var available = "<prefix><yellow>You've reached the stage needed to prestige! Use <white>/prestige</white>."
    private var prestiged = "<prefix><gold><b>PRESTIGE!</b> <yellow>You're now prestige <white><prestige></white> and earned <white><points></white> prestige points."
    private var prestigeTitle = "<gold><b>PRESTIGE <prestige>"
    private var prestigeSubtitle = "<yellow>+<points> prestige points"
    private var upgradeBought = "<prefix><green>Upgraded <upgrade> <green>to level <white><level></white>."
    private var upgradeMaxed = "<prefix><gray>That upgrade is already maxed."
    private var notEnoughPoints = "<prefix><red>You need <white><cost></white> prestige points for that."
    private var failed = "<prefix><red>Something went wrong, please try again."

    fun committed(amount: Double, resource: String) = renderMessage(
        committed,
        Placeholder.unparsed("amount", amount.formatSuffixed()),
        Placeholder.parsed("resource", resource),
    )

    fun nothingToCommit(resource: String) = renderMessage(nothingToCommit, Placeholder.parsed("resource", resource))

    fun notReady() = renderMessage(notReady)

    fun maxPrestige() = renderMessage(maxPrestige)

    fun available() = renderMessage(available)

    fun prestiged(prestige: Int, points: Int) = renderMessage(
        prestiged,
        Placeholder.unparsed("prestige", prestige.toString()),
        Placeholder.unparsed("points", points.toString()),
    )

    fun prestigeTitle(prestige: Int) = renderMessage(prestigeTitle, Placeholder.unparsed("prestige", prestige.toString()))

    fun prestigeSubtitle(points: Int) = renderMessage(prestigeSubtitle, Placeholder.unparsed("points", points.toString()))

    fun upgradeBought(upgrade: String, level: Int) = renderMessage(
        upgradeBought,
        Placeholder.parsed("upgrade", upgrade),
        Placeholder.unparsed("level", level.toString()),
    )

    fun upgradeMaxed() = renderMessage(upgradeMaxed)

    fun notEnoughPoints(cost: Int) = renderMessage(notEnoughPoints, Placeholder.unparsed("cost", cost.toString()))

    fun failed() = renderMessage(failed)
}

@ConfigSerializable
class GeneratorMessages {
    private var inserted = "<prefix><green>Added <blueprint> <green>to generator <white>#<slot></white>."
    private var removed = "<prefix><green>Took <blueprint> <green>out of generator <white>#<slot></white> with <white><uses></white> uses left."
    private var usedUp = "<prefix><yellow>Your <blueprint> <yellow>in generator <white>#<slot></white> has run out of uses."
    private var slotLocked = "<prefix><red>That generator is locked."
    private var slotBusy = "<prefix><red>That generator already has a blueprint."
    private var holdBlueprint = "<prefix><yellow>Hold a blueprint and right-click the generator to add it."
    private var sneakToRemove = "<prefix><yellow>Shift + right-click the generator to take the blueprint out."
    private var resourceLocked = "<prefix><red>You haven't unlocked <resource> <red>yet, so its blueprint can't be used."
    private var noBlueprints = "<prefix><red>You don't have any blueprints in your inventory."
    private var duplicate = "<prefix><red>That blueprint is already in use and can't be added."
    private var unknownBlueprint = "<prefix><red>There's no blueprint called <id>."
    private var blueprintGiven = "<prefix><green>Gave <blueprint> <green>with <white><uses></white> uses to <white><player></white>."
    private var blueprintReceived = "<prefix><green>You received <blueprint> <green>with <white><uses></white> uses!"
    private var invalidUses = "<prefix><red>Uses must be between 1 and <max>."
    private var failed = "<prefix><red>Something went wrong, please try again."

    fun inserted(blueprint: String, slot: Int) = renderMessage(
        inserted,
        Placeholder.parsed("blueprint", blueprint),
        Placeholder.unparsed("slot", slot.toString()),
    )

    fun removed(blueprint: String, slot: Int, uses: Int) = renderMessage(
        removed,
        Placeholder.parsed("blueprint", blueprint),
        Placeholder.unparsed("slot", slot.toString()),
        Placeholder.unparsed("uses", uses.toString()),
    )

    fun usedUp(blueprint: String, slot: Int) = renderMessage(
        usedUp,
        Placeholder.parsed("blueprint", blueprint),
        Placeholder.unparsed("slot", slot.toString()),
    )

    fun slotLocked() = renderMessage(slotLocked)

    fun slotBusy() = renderMessage(slotBusy)

    fun holdBlueprint() = renderMessage(holdBlueprint)

    fun sneakToRemove() = renderMessage(sneakToRemove)

    fun resourceLocked(resource: String) = renderMessage(resourceLocked, Placeholder.parsed("resource", resource))

    fun noBlueprints() = renderMessage(noBlueprints)

    fun duplicate() = renderMessage(duplicate)

    fun unknownBlueprint(id: String) = renderMessage(unknownBlueprint, Placeholder.unparsed("id", id))

    fun blueprintGiven(blueprint: String, uses: Int, player: String) = renderMessage(
        blueprintGiven,
        Placeholder.parsed("blueprint", blueprint),
        Placeholder.unparsed("uses", uses.toString()),
        Placeholder.unparsed("player", player),
    )

    fun blueprintReceived(blueprint: String, uses: Int) = renderMessage(
        blueprintReceived,
        Placeholder.parsed("blueprint", blueprint),
        Placeholder.unparsed("uses", uses.toString()),
    )

    fun invalidUses(max: Int) = renderMessage(invalidUses, Placeholder.unparsed("max", max.toString()))

    fun failed() = renderMessage(failed)
}
