package com.kazakago.swr.compose

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazakago.swr.compose.internal.swrInfiniteSaver
import com.kazakago.swr.compose.internal.toSWRInfiniteState
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRInfinite
import com.kazakago.swr.runtime.internal.NetworkMonitor
import com.kazakago.swr.runtime.internal.buildNetworkMonitor
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map

/**
 * Fetches paginated data, returning the result as Compose state.
 *
 * Equivalent to React SWR's `useSWRInfinite`. Load more pages by incrementing
 * [SWRInfiniteState.size] via [SWRInfiniteState.setSize].
 *
 * @param getKey Returns the cache key for a page given its index and the previous page's data.
 *               Return `null` to stop loading further pages.
 * @param fetcher Suspending function that fetches data for a single page key.
 * @param persister Optional persistence layer for cross-session caching.
 * @param scope CoroutineScope for revalidation jobs. Defaults to the current composition scope.
 * @param config Additional configuration options (e.g. [SWRConfig.initialSize], [SWRConfig.parallel]),
 *               merged with [LocalSWRConfig].
 * @return The current [SWRInfiniteState] as Compose state.
 */
@Composable
public fun <KEY : Any, DATA> rememberSWRInfinite(
    getKey: (pageIndex: Int, previousPageData: DATA?) -> KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    @VisibleForTesting networkMonitor: NetworkMonitor? = null,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWRInfiniteState<DATA> {
    return if (!LocalInspectionMode.current) {
        val cacheOwner = LocalSWRCacheOwner.current
        val defaultConfig = LocalSWRConfig.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val networkMonitor = networkMonitor ?: buildNetworkMonitor()
        val swr = rememberSaveable(
            saver = swrInfiniteSaver(
                getKey = getKey,
                fetcher = fetcher,
                lifecycleOwner = lifecycleOwner,
                scope = scope,
                persister = persister,
                cacheOwner = cacheOwner,
                networkMonitor = networkMonitor,
                defaultConfig = defaultConfig,
                config = config,
            )
        ) {
            SWRInfinite(
                getKey = getKey,
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
            .map { it.toSWRInfiniteState(swr.mutate, swr.getSize(), swr::setSize) }
            .collectAsStateWithLifecycle(SWRInfiniteState.initialize())
            .value
    } else {
        SWRInfiniteState.initialize()
    }
}
