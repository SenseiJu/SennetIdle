package gg.idlemob.idlemobcore.framework.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

private val miniMessage = MiniMessage.miniMessage()

fun mini(template: String, vararg resolvers: TagResolver): Component {
    return miniMessage.deserialize(template, *resolvers).decoration(TextDecoration.ITALIC, false)
}
