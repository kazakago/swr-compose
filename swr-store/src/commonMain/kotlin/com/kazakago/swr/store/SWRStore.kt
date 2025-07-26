package com.kazakago.swr.store

import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.internal.DataSelector
import com.kazakago.swr.store.internal.DataState
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public fun <KEY : Any, DATA> SWRStore(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
): SWRStore<KEY, DATA> = SWRStore(
    key = { key },
    fetcher = fetcher,
    persister = persister,
    cacheOwner = cacheOwner,
)

public class SWRStore<KEY : Any, DATA>(
    public val key: () -> KEY?,
    private val fetcher: suspend (key: KEY) -> DATA,
    private val persister: Persister<KEY, DATA>? = null,
    private val cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
) {
    private val dataSelector = DataSelector(
        getDataFromRemote = {
            val key = runCatching { key() }.getOrNull()
            if (key != null) {
                fetcher(key)
            } else {
                throw IllegalStateException("key is null")
            }
        },
        getDataFromLocal = {
            val key = runCatching { key() }.getOrNull()
            if (key != null) {
                cacheOwner.getOrPut(key).syncData {
                    persister?.loadData(key)
                }
            } else {
                null
            }
        },
        putDataToLocal = { data ->
            val key = runCatching { key() }.getOrNull()
            if (key != null) {
                cacheOwner.getOrPut(key).data = data
                persister?.saveData(key, data)
            }
        },
        getStateFlow = {
            val key = runCatching { key() }.getOrNull()
            if (key != null) {
                cacheOwner.getOrPut(key).stateMapFlow
            } else {
                flowOf(DataState.initialize())
            }
        },
        getState = {
            val key = runCatching { key() }.getOrNull()
            if (key != null) {
                cacheOwner.getOrPut(key).stateMapFlow.value
            } else {
                DataState.initialize()
            }
        },
        putState = { state ->
            val key = runCatching { key() }.getOrNull()
            if (key != null) {
                cacheOwner.getOrPut(key).stateMapFlow.value = state
            }
        },
    )

    public val flow: Flow<SWRStoreState<DATA>> = dataSelector.flow

    public suspend fun get(from: GettingFrom = GettingFrom.Both): Result<DATA> = dataSelector.get(from)

    public suspend fun validate(): Result<DATA> {
        return dataSelector.validate()
    }

    public suspend fun refresh(): Result<DATA> {
        return dataSelector.refresh()
    }

    public suspend fun update(data: DATA?) {
        dataSelector.update(data)
    }

    public suspend fun clear() {
        dataSelector.clear()
    }
}
