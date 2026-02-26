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

public class SWR<KEY : Any, DATA>(
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
    private val resolvedConfig: SWRConfig<KEY, DATA> = SWRConfig<KEY, DATA>(defaultConfig).apply(config)

    private val swrInternal = if (key != null) {
        SWRInternal(
            store = SWRStore(key, fetcher, persister, cacheOwner),
            lifecycleOwner = lifecycleOwner,
            scope = scope,
            networkMonitor = networkMonitor,
            config = resolvedConfig,
        )
    } else {
        null
    }

    public val keepPreviousData: Boolean = resolvedConfig.keepPreviousData

    public val stateFlow: StateFlow<SWRStoreState<DATA>> = swrInternal?.stateFlow ?: MutableStateFlow(SWRStoreState.initialize())

    public val mutate: SWRMutate<DATA> = SWRMutate(
        get = { from -> swrInternal?.store?.get(from) ?: Result.failure(IllegalStateException("key is null")) },
        validate = { swrInternal?.validate() ?: Result.failure(IllegalStateException("key is null")) },
        update = { data, keepState -> swrInternal?.store?.update(data, keepState) },
    )
}
