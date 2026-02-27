package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazakago.swr.runtime.SWRMutation
import com.kazakago.swr.runtime.SWRMutationConfig
import com.kazakago.swr.store.persister.Persister

/**
 * Returns a [SWRMutationState] for manually triggering data mutation.
 *
 * Equivalent to React SWR's `useSWRMutation`. Unlike [rememberSWR], no fetch is triggered
 * automatically; call [SWRMutationState.trigger] to initiate the mutation.
 *
 * @param key The cache key targeted by this mutation. Pass `null` to disable mutation.
 * @param fetcher Suspending function that performs the mutation given a key and an argument.
 * @param persister Optional persistence layer for cross-session caching.
 * @param config Mutation-specific configuration options.
 * @return [SWRMutationState] exposing [trigger][SWRMutationState.trigger], mutation state, and [reset][SWRMutationState.reset].
 */
@Composable
public fun <KEY : Any, DATA, ARG> rememberSWRMutation(
    key: KEY?,
    fetcher: suspend (key: KEY, arg: ARG) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    config: SWRMutationConfig<KEY, DATA>.() -> Unit = {},
): SWRMutationState<KEY, DATA, ARG> {
    val cacheOwner = LocalSWRCacheOwner.current

    val mutation = remember(key) {
        SWRMutation(
            key = key,
            fetcher = fetcher,
            persister = persister,
            cacheOwner = cacheOwner,
            config = config,
        )
    }

    val isMutating by mutation.isMutating.collectAsStateWithLifecycle()
    val data by mutation.data.collectAsStateWithLifecycle()
    val error by mutation.error.collectAsStateWithLifecycle()

    return SWRMutationState(
        trigger = mutation.trigger,
        isMutating = isMutating,
        data = data,
        error = error,
        reset = mutation::reset,
    )
}
