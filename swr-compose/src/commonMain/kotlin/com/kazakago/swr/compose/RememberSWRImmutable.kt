package com.kazakago.swr.compose

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazakago.swr.compose.internal.toSWRState
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRImmutable
import com.kazakago.swr.runtime.internal.NetworkMonitor
import com.kazakago.swr.runtime.internal.buildNetworkMonitor
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map

/**
 * Fetches data for [key] once and never revalidates automatically.
 *
 * Equivalent to React SWR's `useSWRImmutable`. Use this for static or rarely changing data.
 * [SWRState.mutate] can still be called to trigger a manual refresh.
 *
 * @param key The cache key. Pass `null` to suspend fetching until a non-null key is provided.
 * @param fetcher Suspending function that fetches data for [key].
 * @param persister Optional persistence layer for cross-session caching.
 * @param scope CoroutineScope for the initial fetch. Defaults to the current composition scope.
 * @param config Additional configuration options, merged with [LocalSWRConfig].
 * @return The current [SWRState] as Compose state.
 */
@Composable
public fun <KEY : Any, DATA> rememberSWRImmutable(
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
        val swr = remember(key) {
            SWRImmutable(
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
        swr.stateFlow
            .map { it.toSWRState(swr.mutate) }
            .collectAsStateWithLifecycle(SWRState.initialize())
            .value
    } else {
        SWRState.initialize()
    }
}
