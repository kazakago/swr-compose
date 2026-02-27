package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRPreload
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope

/**
 * Returns a [SWRPreload] that prefetches data for [key] on demand.
 *
 * Equivalent to React SWR's `preload`. The returned instance is a suspending operator
 * function that, when invoked, fetches data and stores it in the cache without binding
 * to the composable's lifecycle.
 *
 * @param key The cache key. Pass `null` to disable prefetching.
 * @param fetcher Suspending function that fetches data for [key].
 * @param persister Optional persistence layer for cross-session caching.
 * @param scope CoroutineScope for the prefetch job. Defaults to the current composition scope.
 * @param config Additional configuration options, merged with [LocalSWRConfig].
 * @return A [SWRPreload] instance that can be invoked to trigger the prefetch.
 */
@Composable
public fun <KEY : Any, DATA> rememberSWRPreload(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWRPreload<KEY, DATA> {
    val cacheOwner = LocalSWRCacheOwner.current
    val defaultConfig = LocalSWRConfig.current
    return remember(key) {
        SWRPreload(
            key = key,
            fetcher = fetcher,
            scope = scope,
            persister = persister,
            cacheOwner = cacheOwner,
            defaultConfig = defaultConfig,
            config = config,
        )
    }
}
