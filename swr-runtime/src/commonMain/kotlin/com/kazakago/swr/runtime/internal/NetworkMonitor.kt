package com.kazakago.swr.runtime.internal

import kotlinx.coroutines.flow.Flow

internal expect fun buildNetworkMonitor(): NetworkMonitor

internal interface NetworkMonitor {
    val isOnline: Boolean
    val onlineStatusFlow: Flow<Boolean>
}
