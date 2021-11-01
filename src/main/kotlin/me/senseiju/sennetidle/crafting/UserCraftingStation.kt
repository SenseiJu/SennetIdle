package me.senseiju.sennetidle.crafting

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import com.gmail.filoghost.holographicdisplays.api.line.TextLine
import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sennetidle.serviceProvider
import me.senseiju.sennetidle.users.User
import me.senseiju.sennetidle.utils.extensions.appendItemLine
import me.senseiju.sentils.extensions.primitives.color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

private val plugin = JavaPlugin.getPlugin(SennetIdle::class.java)

private val craftingService = serviceProvider.get<CraftingService>()

class UserCraftingStation(
    private val user: User,
    location: Location
) : BukkitRunnable() {
    private var activeCraftingReagent: CraftingReagent? = null
    
    private val hologram = HologramsAPI.createHologram(plugin, location.clone().add(0.5, 3.0, 0.5))

    init {
        hologram.visibilityManager.isVisibleByDefault = false
        showHologram()
        updateHologram()

        runTaskTimerAsynchronously(plugin, 1L, 1L)
    }

    override fun run() {
        if (activeCraftingReagent == null) {
            return
        }

        if (activeCraftingReagent?.finished == true) {
            getNextCraftingReagent()
            return
        } else {
            activeCraftingReagent?.subtractTimeRemaining()
        }

        updateHologramTimeRemainingLine()
    }

    fun showHologram() {
        user.getBukkitPlayer()?.let {
            hologram.visibilityManager.showTo(it)
        }
    }

    fun getActiveReagent() = activeCraftingReagent?.getReagent()

    fun setActiveCraftingReagent(craftingReagent: CraftingReagent?) {
        activeCraftingReagent = craftingReagent

        updateHologram()
    }

    private fun getNextCraftingReagent() {
        activeCraftingReagent?.getReagent()?.let {
            val nextCraftingReagent = craftingService.getNextCraftingReagent(it, user)
            if (nextCraftingReagent == null) {
                updateHologramTimeRemainingLine()
            } else {
                activeCraftingReagent = nextCraftingReagent
            }
        }
    }

    private fun updateHologram() {
        hologram.clearLines()

        hologram.appendTextLine("&e${activeCraftingReagent?.getReagent()?.displayName ?: "&cNot crafting"}".color())
        hologram.appendTextLine("")
        hologram.appendTextLine(timeRemainingHologramText())
        hologram.appendTextLine("")
        hologram.appendItemLine(activeCraftingReagent?.getReagent()?.material ?: Material.BARRIER)
    }

    private fun updateHologramTimeRemainingLine() {
        (hologram.getLine(2) as TextLine).text = timeRemainingHologramText()
    }

    private fun timeRemainingHologramText() = "&bTime remaining: &e${activeCraftingReagent?.timeRemainingString() ?: "&cNot crafting"}".color()

    fun dispose() {
        hologram.delete()
    }
}