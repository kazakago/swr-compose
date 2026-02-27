package com.kazakago.swr.store

import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.internal.DataSelector
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Low-level cache store that coordinates data fetching and caching for a single key.
 *
 * Manages the full lifecycle of a cache entry: initial load, background revalidation,
 * optimistic updates, and optional persistence. Higher-level classes such as [SWR][com.kazakago.swr.runtime.SWR]
 * and [SWRInfinite][com.kazakago.swr.runtime.SWRInfinite] compose [SWRStore] internally.
 *
 * @param key The cache key identifying this store's entry.
 * @param fetcher Suspending function that retrieves data from a remote source.
 * @param persister Optional persistence layer for cross-session caching.
 * @param cacheOwner Cache namespace. Defaults to the global [defaultSWRCacheOwner].
 */
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

    /** Emits a signal whenever a revalidation has been externally requested via [requestRevalidation]. */
    public val revalidationSignal: SharedFlow<Unit> = cacheOwner.getOrPut(key).revalidationSignal

    /** Emits a revalidation signal to all observers of [revalidationSignal]. */
    public suspend fun requestRevalidation() {
        cacheOwner.getOrPut(key).requestRevalidation()
    }

    /** A flow that emits the current [SWRStoreState] whenever the cache state changes. */
    public val flow: Flow<SWRStoreState<DATA>> = dataSelector.flow

    /**
     * Retrieves the current data.
     *
     * @param from Specifies the source to read from. Defaults to [GettingFrom.Both].
     */
    public suspend fun get(from: GettingFrom = GettingFrom.Both): Result<DATA> = dataSelector.get(from)

    /**
     * Triggers a revalidation (re-fetch) of the data.
     * Skipped if a fetch is already in progress for this key.
     */
    public suspend fun validate(): Result<DATA> {
        return dataSelector.validate()
    }

    /**
     * Forces a fresh fetch from the remote source, bypassing the deduplication interval.
     */
    public suspend fun refresh(): Result<DATA> {
        return dataSelector.refresh()
    }

    /**
     * Updates the cached data directly without triggering a remote fetch.
     *
     * @param data The new data to store, or `null` to clear the cached value.
     * @param keepState If `true`, the current loading/error state is preserved after the update.
     */
    public suspend fun update(data: DATA?, keepState: Boolean = false) {
        dataSelector.update(data, keepState)
    }

    /** Clears the cached data and resets the store to the initial loading state. */
    public suspend fun clear() {
        dataSelector.clear()
    }
}
