package com.kazakago.swr.runtime

import com.kazakago.swr.runtime.internal.SWRValidate
import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope

/**
 * Prefetches data for [key] and stores the result in the SWR cache.
 *
 * Unlike [SWR], this is not bound to a lifecycle; the fetch is triggered on demand by invoking
 * the returned instance. Equivalent to React SWR's `preload`.
 * Used internally by [rememberSWRPreload][com.kazakago.swr.compose.rememberSWRPreload].
 *
 * @param key The cache key. Pass `null` to disable prefetching.
 * @param fetcher Suspending function that fetches data for [key].
 * @param scope CoroutineScope for the prefetch job.
 * @param persister Optional persistence layer for cross-session caching.
 * @param cacheOwner Cache namespace. Defaults to [defaultSWRCacheOwner].
 * @param config Additional configuration options, merged with [defaultConfig].
 */
public class SWRPreload<KEY : Any, DATA>(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    defaultConfig: SWRConfig<Any, Any> = defaultSWRConfig,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
) {
    private val validate: SWRValidate<KEY, DATA>? = if (key != null) {
        SWRValidate(
            store = SWRStore(key, fetcher, persister, cacheOwner),
            scope = scope,
            config = SWRConfig<KEY, DATA>(defaultConfig).apply(config),
        )
    } else {
        null
    }

    /**
     * Triggers the prefetch and returns the result.
     *
     * Within the [dedupingInterval][SWRConfig.dedupingInterval], subsequent calls return
     * the cached result immediately without issuing a new network request.
     */
    public suspend operator fun invoke(): Result<DATA> {
        return validate?.invoke() ?: Result.failure(IllegalStateException("key is null"))
    }
}
