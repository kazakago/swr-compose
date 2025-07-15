package com.kazakago.swr.runtime.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

data class TestNetworkMonitor(
    val onlineStatus: MutableStateFlow<Boolean> = MutableStateFlow(true),
) : NetworkMonitor {
    override val isOnline: Boolean = onlineStatus.value
    override val onlineStatusFlow: Flow<Boolean> = onlineStatus.drop(1)
}
