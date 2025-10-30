package com.kazakago.swr.runtime.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

class TestNetworkMonitor(
    initialState: Boolean = true,
) : NetworkMonitor {
    val onlineStatus: MutableStateFlow<Boolean> = MutableStateFlow(initialState)
    override val onlineStatusFlow: Flow<Boolean> = onlineStatus.drop(1)
    override fun isOnline(): Boolean = onlineStatus.value
}
