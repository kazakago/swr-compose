package com.kazakago.swr.compose.internal

import androidx.annotation.VisibleForTesting

@VisibleForTesting
internal var GlobalNetworkMonitor: NetworkMonitor
    set(value) {
        internalNetworkMonitor = value
    }
    get() {
        var localKonnection = internalNetworkMonitor
        if (localKonnection == null) {
            localKonnection = buildNetworkMonitor()
            internalNetworkMonitor = localKonnection
        }
        return localKonnection
    }
private var internalNetworkMonitor: NetworkMonitor? = null
