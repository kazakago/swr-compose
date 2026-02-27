package com.kazakago.swr.runtime

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.kazakago.swr.runtime.internal.NetworkMonitor
import com.kazakago.swr.runtime.internal.SWRInternal
import com.kazakago.swr.runtime.internal.buildNetworkMonitor
import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.SWRStoreState
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Like [SWR], but disables all automatic revalidation after the initial fetch.
 *
 * Equivalent to React SWR's `useSWRImmutable`. Suitable for data that does not change,
 * such as static configuration or user constants.
 * Used internally by [rememberSWRImmutable][com.kazakago.swr.compose.rememberSWRImmutable].
 *
 * @param key The cache key. Pass `null` to suspend fetching.
 * @param fetcher Suspending function that fetches data for [key].
 * @param lifecycleOwner Lifecycle used to scope the initial fetch.
 * @param scope CoroutineScope for the fetch job.
 * @param persister Optional persistence layer for cross-session caching.
 * @param cacheOwner Cache namespace. Defaults to [defaultSWRCacheOwner].
 * @param config Additional configuration options, merged with [defaultConfig].
 */
public class SWRImmutable<KEY : Any, DATA>(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    @VisibleForTesting networkMonitor: NetworkMonitor = buildNetworkMonitor(),
    defaultConfig: SWRConfig<Any, Any> = defaultSWRConfig,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
) {
    private val swrInternal = if (key != null) {
        SWRInternal(
            store = SWRStore(key, fetcher, persister, cacheOwner),
            lifecycleOwner = lifecycleOwner,
            scope = scope,
            networkMonitor = networkMonitor,
            config = SWRConfig<KEY, DATA>(defaultConfig).apply {
                config()
                revalidateIfStale = false
                revalidateOnFocus = false
                revalidateOnReconnect = false
            },
        )
    } else {
        null
    }

    /** Flow of the current cache state for this key. */
    public val stateFlow: StateFlow<SWRStoreState<DATA>> = swrInternal?.stateFlow ?: MutableStateFlow(SWRStoreState.initialize())

    /** Handle for programmatically mutating the cache entry for this key. */
    public val mutate: SWRMutate<DATA> = if (swrInternal != null) {
        SWRMutate(
            get = swrInternal.store::get,
            validate = { swrInternal.validate() },
            update = swrInternal.store::update,
        )
    } else {
        SWRMutate(
            get = { Result.failure(IllegalStateException("key is null")) },
            validate = { Result.failure(IllegalStateException("key is null")) },
            update = { _, _ -> },
        )
    }
}
