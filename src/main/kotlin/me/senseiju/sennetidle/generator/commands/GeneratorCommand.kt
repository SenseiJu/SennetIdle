package me.senseiju.sennetidle.generator.commands

import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.base.CommandBase
import me.senseiju.sennetidle.PERMISSION_DEV
import me.senseiju.sennetidle.generator.GeneratorService
import org.bukkit.entity.Player

@Command("Generator")
class GeneratorCommand(
    private val generatorService: GeneratorService
) : CommandBase() {

    @SubCommand("Set")
    @Permission(PERMISSION_DEV)
    fun set(sender: Player, id: String) {
        if (generatorService.doesGeneratorExistsWithId(id)) {
            println("Generator already exists")
            return
        }

        val block = sender.rayTraceBlocks(3.0)?.hitBlock
        if (block == null) {
            println("Block not found")
            return
        }

        generatorService.createNewGenerator(id, block.location)
    }

    @SubCommand("Remove")
    @Permission(PERMISSION_DEV)
    fun remove(sender: Player) {
        val block = sender.rayTraceBlocks(3.0)?.hitBlock
        if (block == null) {
            println("Block not found")
            return
        }

        val generator = generatorService.getGeneratorByLocation(block.location) ?: return

        generatorService.deleteGenerator(generator.id)
    }
}