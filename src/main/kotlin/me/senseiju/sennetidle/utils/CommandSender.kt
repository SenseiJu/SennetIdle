package me.senseiju.sennetidle.utils

import me.senseiju.sennetidle.Message
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

private val mm = MiniMessage.miniMessage()

fun CommandSender.message(msg: Message, vararg tagResolvers: TagResolver.Single = emptyArray()) {
    msg.strings.map { mm.deserialize(it, *tagResolvers) }
        .forEach(this::sendMessage)
}
