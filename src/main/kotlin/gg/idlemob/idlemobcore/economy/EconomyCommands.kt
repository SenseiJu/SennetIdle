package gg.idlemob.idlemobcore.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import gg.idlemob.idlemobcore.framework.extensions.parseSuffixed
import gg.idlemob.idlemobcore.framework.logging.EventLogger
import gg.idlemob.idlemobcore.plugin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private val messages
    get() = plugin.messages.economy

private fun findPlayer(name: String): OfflinePlayer? = Bukkit.getPlayerExact(name) ?: Bukkit.getOfflinePlayerIfCached(name)

private fun parseAmount(sender: CommandSender, input: String, currency: Currency): Double? {
    val amount = input.parseSuffixed()?.let(currency::round)?.takeIf { it > 0 && it.isFinite() }
    if (amount == null) {
        sender.sendMessage(messages.invalidAmount(input))
    }
    return amount
}

private fun parseCurrency(sender: CommandSender, input: String?): Currency? {
    if (input == null) {
        return Currency.MONEY
    }
    return Currency.byId(input) ?: run {
        sender.sendMessage(messages.unknownCurrency(input))
        null
    }
}

private fun runAsync(sender: CommandSender, block: suspend () -> List<Component>) {
    plugin.scope.launch {
        val replies = try {
            block()
        } catch (ex: InsufficientFundsException) {
            listOf(messages.notEnough(ex.currency))
        } catch (ex: UnknownAccountException) {
            listOf(messages.unknownPlayer(Bukkit.getOfflinePlayer(ex.uuid).name ?: ex.uuid.toString()))
        } catch (ex: Exception) {
            EventLogger.error("economy_command_failed", ex) { "sender" to sender.name }
            listOf(messages.failed())
        }
        withContext(plugin.scope.main) { replies.forEach(sender::sendMessage) }
    }
}

@CommandAlias("balance|bal|money")
class BalanceCommand(private val economy: EconomyService) : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    fun balance(sender: CommandSender, @Optional targetName: String?) {
        val target = when {
            targetName != null -> findPlayer(targetName)
            sender is Player -> sender
            else -> null
        }
        if (target == null) {
            sender.sendMessage(messages.unknownPlayer(targetName ?: "?"))
            return
        }

        runAsync(sender) {
            val name = target.name ?: targetName ?: "?"
            val lines = Currency.entries.map { currency ->
                val amount = economy.balance(target.uniqueId, currency) ?: return@runAsync listOf(messages.unknownPlayer(name))
                messages.balanceLine(currency, amount)
            }
            listOf(messages.balanceHeader(name)) + lines
        }
    }
}

@CommandAlias("pay")
class PayCommand(private val economy: EconomyService) : BaseCommand() {

    @Default
    @CommandCompletion("@players @nothing @currencies")
    fun pay(player: Player, targetName: String, amountInput: String, @Optional currencyInput: String?) {
        val currency = parseCurrency(player, currencyInput) ?: return
        if (!currency.payable) {
            player.sendMessage(messages.notPayable(currency))
            return
        }

        val amount = parseAmount(player, amountInput, currency) ?: return
        val target = findPlayer(targetName)
        if (target == null) {
            player.sendMessage(messages.unknownPlayer(targetName))
            return
        }
        if (target.uniqueId == player.uniqueId) {
            player.sendMessage(messages.paySelf())
            return
        }

        val targetDisplay = target.name ?: targetName
        runAsync(player) {
            economy.transfer(player.uniqueId, target.uniqueId, currency, amount, "pay:${player.uniqueId}->${target.uniqueId}")
            target.player?.sendMessage(messages.received(currency, amount, player.name))
            listOf(messages.paid(currency, amount, targetDisplay))
        }
    }
}

@CommandAlias("baltop|moneytop")
class BaltopCommand(private val economy: EconomyService) : BaseCommand() {

    @Default
    @CommandCompletion("@currencies")
    fun baltop(sender: CommandSender, @Optional currencyInput: String?) {
        val currency = parseCurrency(sender, currencyInput) ?: return

        runAsync(sender) {
            val top = economy.top(currency, TOP_SIZE)
            val lines = top.mapIndexed { index, (uuid, amount) ->
                messages.topLine(index + 1, Bukkit.getOfflinePlayer(uuid).name ?: "Unknown", currency, amount)
            }
            listOf(messages.topHeader(currency)) + lines.ifEmpty { listOf(messages.topEmpty()) }
        }
    }

    private companion object {
        const val TOP_SIZE = 10
    }
}

@CommandAlias("eco")
@CommandPermission("idlemob.admin.eco")
class EcoCommand(private val economy: EconomyService) : BaseCommand() {

    @Subcommand("give")
    @CommandCompletion("@players @currencies")
    fun give(sender: CommandSender, targetName: String, currencyInput: String, amountInput: String) {
        update(sender, targetName, currencyInput, amountInput) { uuid, currency, amount ->
            economy.deposit(uuid, currency, amount, "admin_give:${sender.name}")
        }
    }

    @Subcommand("take")
    @CommandCompletion("@players @currencies")
    fun take(sender: CommandSender, targetName: String, currencyInput: String, amountInput: String) {
        update(sender, targetName, currencyInput, amountInput) { uuid, currency, amount ->
            economy.withdraw(uuid, currency, amount, "admin_take:${sender.name}")
        }
    }

    @Subcommand("set")
    @CommandCompletion("@players @currencies")
    fun set(sender: CommandSender, targetName: String, currencyInput: String, amountInput: String) {
        val currency = parseCurrency(sender, currencyInput) ?: return
        val amount = amountInput.parseSuffixed()?.takeIf { it >= 0 && it.isFinite() }
        if (amount == null) {
            sender.sendMessage(messages.invalidAmount(amountInput))
            return
        }
        val target = findPlayer(targetName) ?: return sender.sendMessage(messages.unknownPlayer(targetName))

        runAsync(sender) {
            val balance = economy.set(target.uniqueId, currency, amount, "admin_set:${sender.name}")
            listOf(messages.adminUpdated(target.name ?: targetName, currency, balance))
        }
    }

    private fun update(
        sender: CommandSender,
        targetName: String,
        currencyInput: String,
        amountInput: String,
        action: suspend (java.util.UUID, Currency, Double) -> Double,
    ) {
        val currency = parseCurrency(sender, currencyInput) ?: return
        val amount = parseAmount(sender, amountInput, currency) ?: return
        val target = findPlayer(targetName) ?: return sender.sendMessage(messages.unknownPlayer(targetName))

        runAsync(sender) {
            val balance = try {
                action(target.uniqueId, currency, amount)
            } catch (ex: UnknownAccountException) {
                return@runAsync listOf(messages.unknownPlayer(targetName))
            }
            listOf(messages.adminUpdated(target.name ?: targetName, currency, balance))
        }
    }
}
