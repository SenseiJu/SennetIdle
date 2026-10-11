package gg.idlemob.idlemobcore.framework.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

class MainThreadDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !plugin.server.isPrimaryThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (plugin.isEnabled) {
            plugin.server.scheduler.runTask(plugin, block)
        } else {
            block.run()
        }
    }
}
