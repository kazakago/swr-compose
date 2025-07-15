package com.kazakago.swr.runtime.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class TestNetworkMonitor(
    override val isOnline: Boolean = true,
    override val onlineStatusFlow: Flow<Boolean> = flowOf(),
) : NetworkMonitor
