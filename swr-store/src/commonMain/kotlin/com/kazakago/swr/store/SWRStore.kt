package com.kazakago.swr.store

import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.internal.DataSelector
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.flow.Flow

public class SWRStore<KEY : Any, DATA>(
    public val key: KEY,
    private val fetcher: suspend (key: KEY) -> DATA,
    private val persister: Persister<KEY, DATA>? = null,
    private val cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
) {
    private val dataSelector = DataSelector(
        getDataFromRemote = {
            fetcher(key)
        },
        getDataFromLocal = {
            cacheOwner.getOrPut(key).syncData {
                persister?.loadData(key)
            }
        },
        putDataToLocal = { data ->
            cacheOwner.getOrPut(key).data = data
            persister?.saveData(key, data)
        },
        getStateFlow = {
            cacheOwner.getOrPut(key).stateMapFlow
        },
        getState = {
            cacheOwner.getOrPut(key).stateMapFlow.value
        },
        putState = { state ->
            cacheOwner.getOrPut(key).stateMapFlow.value = state
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

    public suspend fun update(data: DATA?, keepState: Boolean = false) {
        dataSelector.update(data, keepState)
    }

    public suspend fun clear() {
        dataSelector.clear()
    }
}
