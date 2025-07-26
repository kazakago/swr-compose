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
import kotlinx.coroutines.flow.StateFlow

public fun <KEY : Any, DATA> SWR(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    @VisibleForTesting networkMonitor: NetworkMonitor = buildNetworkMonitor(),
    defaultConfig: SWRConfig<Any, Any> = defaultSWRConfig,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWR<KEY, DATA> = SWR(
    key = { key },
    fetcher = fetcher,
    lifecycleOwner = lifecycleOwner,
    scope = scope,
    persister = persister,
    cacheOwner = cacheOwner,
    networkMonitor = networkMonitor,
    defaultConfig = defaultConfig,
    config = config,
)

public class SWR<KEY : Any, DATA>(
    key: () -> KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    @VisibleForTesting networkMonitor: NetworkMonitor = buildNetworkMonitor(),
    defaultConfig: SWRConfig<Any, Any> = defaultSWRConfig,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
) {
    private val swrInternal = SWRInternal(
        store = SWRStore(key, fetcher, persister, cacheOwner),
        lifecycleOwner = lifecycleOwner,
        scope = scope,
        networkMonitor = networkMonitor,
        config = SWRConfig<KEY, DATA>(defaultConfig).apply(config),
    )

    public val stateFlow: StateFlow<SWRStoreState<DATA>> = swrInternal.stateFlow

    public val mutate: SWRMutate<DATA> = SWRMutate(
        get = { from -> swrInternal.store.get(from) },
        validate = { swrInternal.validate() },
        update = { data -> swrInternal.store.update(data) },
    )
}
