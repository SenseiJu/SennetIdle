package me.senseiju.sennetidle.users

import me.senseiju.sennetidle.idlemobs.IdleMobService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sentils.extensions.message
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

private val idleMobService = serviceProvider.get<IdleMobService>()

class UserConnectListener(private val userService: UserService) : Listener {
    private val users = userService.users

    @EventHandler
    private fun onPlayerPreLogin(e: AsyncPlayerPreLoginEvent) {
        if (e.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return

        if (!users.containsKey(e.uniqueId)) {
            val user = try {
                userService.createUser(e.uniqueId)
            } catch (ex: Exception) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, FAILED_TO_LOAD_DATA)
                ex.printStackTrace()
                return
            }

            idleMobService.accelerateWaveHandlers(user)
            //generatorService.accelerateGenerators(user)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(e: PlayerJoinEvent) {
        if (!users.containsKey(e.player.uniqueId)) {
            e.player.kick(FAILED_TO_LOAD_DATA)
            return
        }

        val user = userService.getUser(e.player.uniqueId)

        e.joinMessage(null)
        e.player.message("&bWelcome back &e&l${e.player.name}&b, you were idle for &e&l${user.timeLoggedOut()}")
    }

    @EventHandler
    private fun onPlayerQuitEvent(e: PlayerQuitEvent) {
        e.quitMessage(null)

        users[e.player.uniqueId]?.lastSeen = System.currentTimeMillis()
    }
}

private val FAILED_TO_LOAD_DATA =
    text().append(
        text("SennetIdle\n", BLUE, TextDecoration.BOLD),
        text("\n"),
        text("""
            Your player data was not loaded before you connected
            please try and reconnect or contact an admin if this continues
            """.trimIndent(), RED),
    ).asComponent()