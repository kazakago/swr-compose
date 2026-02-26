package com.kazakago.swr.compose

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazakago.swr.compose.internal.toSWRState
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.internal.NetworkMonitor
import com.kazakago.swr.runtime.internal.buildNetworkMonitor
import com.kazakago.swr.runtime.KeepPreviousDataHolder
import com.kazakago.swr.runtime.withKeepPreviousData
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map

@Composable
public fun <KEY : Any, DATA> rememberSWR(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    @VisibleForTesting networkMonitor: NetworkMonitor? = null,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWRState<DATA> {
    return if (!LocalInspectionMode.current) {
        val cacheOwner = LocalSWRCacheOwner.current
        val defaultConfig = LocalSWRConfig.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val networkMonitor = networkMonitor ?: buildNetworkMonitor()
        val previousDataHolder = remember { KeepPreviousDataHolder<DATA>() }
        val swr = remember(key) {
            SWR(
                key = key,
                fetcher = fetcher,
                lifecycleOwner = lifecycleOwner,
                scope = scope,
                persister = persister,
                cacheOwner = cacheOwner,
                networkMonitor = networkMonitor,
                defaultConfig = defaultConfig,
                config = config,
            )
        }
        val flow = if (swr.keepPreviousData) {
            swr.stateFlow.withKeepPreviousData(previousDataHolder)
        } else {
            swr.stateFlow
        }
        flow.map { it.toSWRState(swr.mutate) }
            .collectAsStateWithLifecycle(SWRState.initialize())
            .value
    } else {
        SWRState.initialize()
    }
}
