package com.kazakago.swr.runtime.internal

import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.w3c.dom.events.Event

public actual fun buildNetworkMonitor(): NetworkMonitor = NetworkMonitorImpl()

private class NetworkMonitorImpl : NetworkMonitor {

    override val onlineStatusFlow: Flow<Boolean> = callbackFlow {
        val onlineEvent: ((Event) -> Unit) = { trySend(true) }
        val offlineEvent: ((Event) -> Unit) = { trySend(false) }
        window.addEventListener("online", onlineEvent)
        window.addEventListener("offline", offlineEvent)
        awaitClose {
            window.removeEventListener("online", onlineEvent)
            window.removeEventListener("offline", offlineEvent)
        }
    }.distinctUntilChanged()

    override fun isOnline(): Boolean = window.navigator.onLine
}
