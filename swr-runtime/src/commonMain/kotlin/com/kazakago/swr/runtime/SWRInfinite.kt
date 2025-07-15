package com.kazakago.swr.runtime

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.kazakago.swr.runtime.internal.NetworkMonitor
import com.kazakago.swr.runtime.internal.SWRInternal
import com.kazakago.swr.runtime.internal.buildNetworkMonitor
import com.kazakago.swr.store.GettingFrom
import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.SWRStoreState
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

public class SWRInfinite<KEY : Any, DATA>(
    getKey: (pageIndex: Int, previousPageData: DATA?) -> KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    @VisibleForTesting networkMonitor: NetworkMonitor = buildNetworkMonitor(),
    defaultConfig: SWRConfig<Any, Any> = defaultSWRConfig,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
) {
    private val currentConfig = SWRConfig<KEY, DATA>(defaultConfig).apply(config)
    private var swrList: List<SWRInternal<KEY, DATA>?> = emptyList()
    public var pageSize: MutableStateFlow<Int> = MutableStateFlow(currentConfig.initialSize)
    public var previousFirstKey: KEY? = null

    init {
        scope.launch {
            pageSize.collect { pageSize ->
                swrList = buildList {
                    var previousPageData: DATA? = null
                    repeat(pageSize) { pageIndex ->
                        currentConfig.apply {
                            revalidateIfStale = revalidateIfStale && (currentConfig.revalidateAll || (currentConfig.revalidateFirstPage && pageIndex == 0))
                            revalidateOnFocus = revalidateOnFocus && (currentConfig.revalidateAll || (currentConfig.revalidateFirstPage && pageIndex == 0))
                            revalidateOnReconnect = revalidateOnReconnect && (currentConfig.revalidateAll || (currentConfig.revalidateFirstPage && pageIndex == 0))
                        }
                        val key = getKey(pageIndex, previousPageData)
                        if (!currentConfig.persistSize && pageIndex == 0) {
                            if (previousFirstKey != null && previousFirstKey != key) {
                                this@SWRInfinite.pageSize.value = currentConfig.initialSize
                                previousFirstKey = null
                                return@collect
                            } else {
                                previousFirstKey = key
                            }
                        }
                        if (key != null) {
                            val store = SWRStore(key, fetcher, persister, cacheOwner)
                            previousPageData = store.get(from = GettingFrom.LocalOnly).getOrNull()
                            add(SWRInternal(store, lifecycleOwner, scope, networkMonitor, currentConfig))
                        } else {
                            add(null)
                        }
                    }
                }
                stateFlow = combine(
                    flows = swrList.map { it?.stateFlow ?: MutableStateFlow(SWRStoreState.initialize()) },
                    transform = { it },
                )
                    .map { stateList ->
                        val data = stateList.map { it.data }
                        val error = stateList.firstNotNullOfOrNull { it.error }
                        val isLoading = stateList.any { it.isLoading }
                        if (error != null) {
                            SWRStoreState.Error(data, error)
                        } else if (isLoading) {
                            if (stateList.any { it.data != null }) {
                                SWRStoreState.Loading(data)
                            } else {
                                SWRStoreState.Loading(null)
                            }
                        } else {
                            SWRStoreState.Completed(data)
                        }
                    }
                    .stateIn(
                        scope = scope,
                        started = SharingStarted.Lazily,
                        initialValue = SWRStoreState.initialize(),
                    )
            }
        }
    }

    public var stateFlow: StateFlow<SWRStoreState<List<DATA?>>> = MutableStateFlow(SWRStoreState.initialize())
        private set

    public val mutate: SWRInfiniteMutate<DATA> = SWRInfiniteMutate(
        getSize = { pageSize.value },
        get = { index -> swrList[index]?.store?.get(GettingFrom.LocalOnly) ?: Result.failure(IllegalStateException("key is null")) },
        validate = { index -> swrList[index]?.validate() ?: Result.failure(IllegalStateException("key is null")) },
        update = { index, data -> swrList[index]?.store?.update(data) },
    )

    public fun getSize(): Int {
        return pageSize.value
    }

    public fun setSize(size: Int) {
        pageSize.value = size
    }
}
