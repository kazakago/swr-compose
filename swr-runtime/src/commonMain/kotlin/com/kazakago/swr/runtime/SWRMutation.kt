package com.kazakago.swr.runtime

import com.kazakago.swr.store.GettingFrom
import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides manual data mutation with optimistic update support.
 *
 * Unlike [SWR], data fetching is not triggered automatically; call [trigger] to initiate a mutation.
 * Equivalent to React SWR's `useSWRMutation` hook logic.
 * Used internally by [rememberSWRMutation][com.kazakago.swr.compose.rememberSWRMutation].
 *
 * @param key The cache key targeted by this mutation. Pass `null` to disable mutation.
 * @param fetcher Suspending function that performs the mutation given a key and an argument.
 * @param persister Optional persistence layer for cross-session caching.
 * @param cacheOwner Cache namespace. Defaults to [defaultSWRCacheOwner].
 * @param config Mutation-specific configuration options.
 */
public class SWRMutation<KEY : Any, DATA, ARG>(
    private val key: KEY?,
    fetcher: suspend (key: KEY, arg: ARG) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    config: SWRMutationConfig<KEY, DATA>.() -> Unit = {},
) {
    private val _isMutating = MutableStateFlow(false)
    private val _data: MutableStateFlow<DATA?> = MutableStateFlow(null)
    private val _error: MutableStateFlow<Throwable?> = MutableStateFlow(null)

    /** `true` while a mutation is in progress. */
    public val isMutating: StateFlow<Boolean> = _isMutating.asStateFlow()

    /** The data returned by the last successful mutation, or `null` if none has occurred. */
    public val data: StateFlow<DATA?> = _data.asStateFlow()

    /** The error thrown by the last failed mutation, or `null` if none has occurred. */
    public val error: StateFlow<Throwable?> = _error.asStateFlow()

    private val sharedStore: SWRStore<KEY, DATA>? = if (key != null) {
        SWRStore(
            key = key,
            fetcher = { error("SWRMutation does not validate through this store") },
            persister = persister,
            cacheOwner = cacheOwner,
        )
    } else null

    /** Call this to trigger the mutation. */
    public val trigger: SWRTrigger<KEY, DATA, ARG> = SWRTrigger { arg, overrideConfig ->
        val currentKey = key ?: return@SWRTrigger Result.failure(IllegalStateException("key is null"))

        val currentConfig = SWRMutationConfig<KEY, DATA>().apply(config).apply(overrideConfig)

        _isMutating.value = true
        _error.value = null

        val previousData = sharedStore?.get(GettingFrom.LocalOnly)?.getOrNull()

        currentConfig.optimisticData?.let { sharedStore?.update(it, false) }

        runCatching {
            fetcher(currentKey, arg)
        }.onSuccess { newData ->
            _data.value = newData
            if (currentConfig.populateCache) {
                sharedStore?.update(newData, false)
            } else if (currentConfig.optimisticData != null && !currentConfig.revalidate) {
                sharedStore?.update(previousData, false)
            }
            if (currentConfig.revalidate) {
                sharedStore?.requestRevalidation()
            }
            currentConfig.onSuccess?.invoke(newData, currentKey, currentConfig)
        }.onFailure { e ->
            _error.value = e
            if (currentConfig.rollbackOnError) {
                sharedStore?.update(previousData, true)
            }
            currentConfig.onError?.invoke(e, currentKey, currentConfig)
        }.also {
            _isMutating.value = false
        }
    }

    /** Resets [data], [error], and [isMutating] to their initial values. */
    public fun reset() {
        _isMutating.value = false
        _data.value = null
        _error.value = null
    }
}
