package com.kazakago.swr.compose.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.darwin.dispatch_get_global_queue
import platform.posix.QOS_CLASS_BACKGROUND

public actual fun buildNetworkMonitor(): NetworkMonitor = NetworkMonitorImpl()

private class NetworkMonitorImpl : NetworkMonitor {

    private var path: nw_path_t? = null
    override val onlineStatusFlow: Flow<Boolean> = callbackFlow {
        @OptIn(ExperimentalForeignApi::class)
        val queue = dispatch_get_global_queue(QOS_CLASS_BACKGROUND.convert(), 0.convert())
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            this@NetworkMonitorImpl.path = path
            trySend(isConnected(path))
        }
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.distinctUntilChanged().drop(1)

    override fun isOnline(): Boolean = isConnected(path)

    private fun isConnected(path: nw_path_t): Boolean {
        return nw_path_get_status(path) == nw_path_status_satisfied
    }
}
