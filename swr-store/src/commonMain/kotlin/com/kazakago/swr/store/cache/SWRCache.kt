package com.kazakago.swr.store.cache

import com.kazakago.swr.store.internal.DataState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** The process-wide default [SWRCacheOwner] shared across all SWR instances. */
public val defaultSWRCacheOwner: SWRCacheOwner = SWRCacheOwner()

/**
 * Manages all cache entries within a given scope.
 *
 * Use [LocalSWRCacheOwner][com.kazakago.swr.compose.LocalSWRCacheOwner] in Compose, or provide
 * a custom instance to isolate cache namespaces (e.g. per user session).
 */
public class SWRCacheOwner {
    /** The backing map of cache entries, keyed by SWR key. */
    public val cacheMap: MutableMap<Any, SWRCache> = mutableMapOf()

    /** Returns the [SWRCache] for [key], creating a new entry if one does not exist. */
    public fun <KEY : Any> getOrPut(key: KEY): SWRCache {
        return cacheMap.getOrPut(key) { SWRCache() }
    }

    /** Clears all cached data across every entry in this owner. */
    public fun clearAll() {
        cacheMap.forEach { it.value.clear() }
    }
}

/**
 * Holds the cached state for a single SWR key.
 */
public class SWRCache {
    /** The currently cached data value. Type-erased at this layer. */
    public var data: Any? = null
    internal val stateMapFlow: MutableStateFlow<DataState> = MutableStateFlow(DataState.initialize())
    private val _revalidationSignal: MutableSharedFlow<Unit> = MutableSharedFlow()

    /** A shared flow that emits whenever a revalidation is requested for this cache entry. */
    public val revalidationSignal: SharedFlow<Unit> = _revalidationSignal.asSharedFlow()

    /** Emits a revalidation signal to all current subscribers. */
    public suspend fun requestRevalidation() {
        _revalidationSignal.emit(Unit)
    }

    /** Clears the cached data and resets state to the initial loading state. */
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
