package gg.idlemob.idlemobcore.framework.menu

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem
import xyz.xenondevs.invui.item.ItemProvider

class PageButton(
    private val visible: ItemProvider,
    private val hidden: ItemProvider,
    private val delta: Int,
) : AbstractPagedGuiBoundItem() {

    override fun getItemProvider(viewer: Player): ItemProvider {
        return if (hasTargetPage()) visible else hidden
    }

    override fun handleClick(clickType: ClickType, player: Player, click: Click) {
        if (hasTargetPage()) {
            gui.page += delta
        }
    }

    private fun hasTargetPage(): Boolean {
        return gui.page + delta in 0 until gui.pageCount
    }
}
