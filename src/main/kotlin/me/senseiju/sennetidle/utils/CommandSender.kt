package me.senseiju.sennetidle.utils

import me.senseiju.sennetidle.Message
import me.senseiju.sentils.storage.ConfigFile
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

private val mm = MiniMessage.miniMessage()

private const val NO_MESSAGE_FOUND = "<red><b>Message not found:</b> <yellow>{key}"

object MessageProvider {
    lateinit var config: ConfigFile
}

fun CommandSender.configMiniMessage(key: String, vararg tagResolvers: TagResolver = emptyArray()) {
    if (!MessageProvider.config.isSet(key)) {
        sendMessage(mm.deserialize(NO_MESSAGE_FOUND, Placeholder.parsed("{key}", key)))
    } else if (MessageProvider.config.isString(key)) {
        MessageProvider.config.getString(key, NO_MESSAGE_FOUND).let {
            sendMessage(mm.deserialize(it, *tagResolvers))
        }
    } else {
        MessageProvider.config.getStringList(key)
            .map { mm.deserialize(it, *tagResolvers) }
            .forEach(this::sendMessage)
    }
}

fun CommandSender.miniMessage(msg: Message, vararg tagResolvers: TagResolver.Single = emptyArray()) {
    msg.strings.map { mm.deserialize(it, *tagResolvers) }
        .forEach(this::sendMessage)
}