package gg.idlemob.idlemobcore.framework.coroutines

import gg.idlemob.idlemobcore.framework.logging.EventLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.plugin.Plugin

class PluginScope(plugin: Plugin) : CoroutineScope {
    val main = MainThreadDispatcher(plugin)

    override val coroutineContext = SupervisorJob() + Dispatchers.IO + CoroutineName(plugin.name) +
        CoroutineExceptionHandler { context, ex ->
            EventLogger.error("coroutine_failed", ex) { "coroutine" to context[CoroutineName]?.name }
        }

    fun shutdown() {
        cancel()
    }
}
