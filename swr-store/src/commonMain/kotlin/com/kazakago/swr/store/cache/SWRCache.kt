package com.kazakago.swr.store.cache

import com.kazakago.swr.store.internal.DataState
import kotlinx.coroutines.flow.MutableStateFlow

public val defaultSWRCacheOwner: SWRCacheOwner = SWRCacheOwner()

public class SWRCacheOwner {
    public val cacheMap: MutableMap<Any, SWRCache> = mutableMapOf()

    public fun <KEY : Any> getOrPut(key: KEY): SWRCache {
        return cacheMap.getOrPut(key) { SWRCache() }
    }

    public fun clearAll() {
        cacheMap.forEach { it.value.clear() }
    }
}

public class SWRCache {
    public var data: Any? = null
    internal val stateMapFlow: MutableStateFlow<DataState> = MutableStateFlow(DataState.initialize())

    public fun clear() {
        data = null
        stateMapFlow.value = DataState.initialize()
    }

    internal inline fun <T> syncData(load: () -> T?): T? = syncToCache(
        loadFromCache = { data },
        saveToCache = { data = it },
        loadFromPersister = { load() }
    )

    @Suppress("UNCHECKED_CAST")
    private inline fun <T> syncToCache(loadFromCache: () -> Any?, saveToCache: (T?) -> Unit, loadFromPersister: () -> T?): T? {
        val cacheData = loadFromCache() as T?
        return if (cacheData != null) {
            cacheData
        } else {
            val localData = loadFromPersister()
            if (localData != null) {
                saveToCache(localData)
            }
            localData
        }
    }
}
