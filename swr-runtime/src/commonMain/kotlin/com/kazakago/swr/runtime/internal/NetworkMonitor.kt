package com.kazakago.swr.runtime.internal

import kotlinx.coroutines.flow.Flow

public expect fun buildNetworkMonitor(): NetworkMonitor

public interface NetworkMonitor {
    public fun isOnline(): Boolean
    public val onlineStatusFlow: Flow<Boolean>
}
