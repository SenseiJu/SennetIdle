package me.senseiju.sennetidle.users

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent

class UserListener(private val userService: UserService) : Listener {
    private val users = userService.users

    @EventHandler
    private fun onPlayerPreLogin(e: AsyncPlayerPreLoginEvent) {
        if (e.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return

        if (!users.containsKey(e.uniqueId)) {
            userService.createUser(e.uniqueId)
        }
    }

    @EventHandler
    private fun onPlayerJoin(e: PlayerJoinEvent) {
        if (users.containsKey(e.player.uniqueId)) return

        e.player.kick(FAILED_TO_LOAD_DATA)
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
