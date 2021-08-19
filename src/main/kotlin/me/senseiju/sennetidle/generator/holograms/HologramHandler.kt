package me.senseiju.sennetidle.generator.holograms

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.generator.CraftingGenerator
import me.senseiju.sennetidle.generator.CraftingReagent
import me.senseiju.sennetidle.generator.GeneratorService
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.users.UserService
import me.senseiju.sentils.registerEvents
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

private val userService = serviceProvider.get<UserService>()
private val generatorService = serviceProvider.get<GeneratorService>()

class HologramHandler(private val plugin: SennetIdle) {

    init {
        plugin.registerEvents(HologramListener())
    }

    fun createHologram(player: Player, generator: CraftingGenerator): Hologram {
        val user = userService.getUser(player.uniqueId)
        val hologram = HologramsAPI.createHologram(plugin, generator.location.clone().add(0.5, 2.0, 0.5))
        val craftingReagent = generator.activeUserCrafts[user]

        with (hologram.visibilityManager) {
            showTo(player)
            isVisibleByDefault = false
        }

        hologram.updateGeneratorHologram(craftingReagent)

        return hologram
    }

    fun createHolograms(generator: CraftingGenerator) {
        plugin.server.onlinePlayers.forEach {
            val user = userService.getUser(it.uniqueId)

            generator.userHolograms[user] = createHologram(it, generator)
        }
    }

    fun deleteAllHolograms() {
        generatorService.generators.values.forEach { generator ->
            generator.userHolograms.values.forEach { hologram ->
                hologram.delete()
            }
        }
    }
}

fun Hologram.updateGeneratorHologram(craftingReagent: CraftingReagent?) {
    clearLines()

    appendTextLine(craftingReagent?.reagent?.displayName ?: "Not crafting")
    appendItemLine(craftingReagent?.reagent?.material ?: Material.BARRIER)
}

private fun Hologram.appendItemLine(material: Material): ItemLine {
    return appendItemLine(ItemStack(material))
}