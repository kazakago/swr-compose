package com.kazakago.swr.runtime

import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.SWRStoreState
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

public class SWRSubscription<KEY : Any, DATA>(
    private val key: KEY?,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    subscribe: suspend (key: KEY) -> Flow<DATA>,
) {
    private val _error: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    public val error: StateFlow<Throwable?> = _error.asStateFlow()

    private val sharedStore: SWRStore<KEY, DATA>? = if (key != null) {
        SWRStore(
            key = key,
            fetcher = { error("SWRSubscription does not validate through this store") },
            persister = persister,
            cacheOwner = cacheOwner,
        )
    } else null

    public val stateFlow: StateFlow<SWRStoreState<DATA>> = sharedStore?.flow?.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = SWRStoreState.Loading(null),
    ) ?: MutableStateFlow(SWRStoreState.Loading(null))

    private val job: Job? = if (key != null) {
        scope.launch {
            try {
                subscribe(key).collect { data ->
                    _error.value = null
                    sharedStore?.update(data, false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _error.value = e
            }
        }
    } else null

    /**
     * Cancels the subscription job.
     *
     * When using the default [rememberCoroutineScope][androidx.compose.runtime.rememberCoroutineScope],
     * this is also called automatically when the composable leaves composition.
     * When passing an external scope (e.g. `viewModelScope`), this is called on key change,
     * but NOT when the composable leaves composition — allowing the subscription to outlive
     * the composable.
     */
    public fun cancel() {
        job?.cancel()
    }
}
