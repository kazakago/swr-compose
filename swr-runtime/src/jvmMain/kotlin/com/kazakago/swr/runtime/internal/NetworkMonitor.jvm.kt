package com.kazakago.swr.runtime.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.isActive
import java.net.NetworkInterface
import kotlin.time.Duration.Companion.seconds

public actual fun buildNetworkMonitor(): NetworkMonitor = NetworkMonitorImpl()

private class NetworkMonitorImpl : NetworkMonitor {

    companion object {
        private val wifiNetworkInterfaces = listOf("wi-fi", "wireless", "en0", "wlan0", "ap0")
        private val ethernetNetworkInterfaces = listOf("ethernet", "lan", "en1", "eth0", "eth1")
    }

    override val onlineStatusFlow: Flow<Boolean> = callbackFlow {
        while (isActive) {
            delay(1.seconds)
            trySend(isConnected())
        }
    }.distinctUntilChanged().drop(1)

    override fun isOnline(): Boolean = isConnected()

    private fun isConnected(): Boolean = try {
        NetworkInterface.getNetworkInterfaces().toList()
            .any { networkInterface ->
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    false
                } else if (wifiNetworkInterfaces.any { it.contains(networkInterface.name.lowercase()) }) {
                    true
                } else if (ethernetNetworkInterfaces.any { it.contains(networkInterface.name.lowercase()) }) {
                    true
                } else {
                    false
                }
            }
    } catch (_: Throwable) {
        false
    }
}
