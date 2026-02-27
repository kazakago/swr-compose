package com.kazakago.swr.runtime

import com.kazakago.swr.store.GettingFrom

/**
 * Handle for programmatically mutating the SWR cache for a single key.
 *
 * Obtained from [SWRState.mutate][com.kazakago.swr.compose.SWRState.mutate] or [SWR.mutate].
 * Equivalent to the bound `mutate` function returned by React SWR's `useSWR`.
 */
public data class SWRMutate<DATA>(
    private val get: suspend (from: GettingFrom) -> Result<DATA>,
    private val validate: suspend () -> Result<DATA>,
    private val update: suspend (newData: DATA?, keepState: Boolean) -> Unit,
) {

    /**
     * Mutates the cached data.
     *
     * @param data Optional suspending function that produces the new data.
     *             If `null`, only a revalidation is triggered without updating the cache value.
     * @param config Mutation options such as optimistic updates, rollback on error, etc.
     * @return The result of the mutation, wrapping the new data or `null` if [data] was `null`.
     */
    public suspend operator fun invoke(
        data: (suspend () -> DATA)? = null,
        config: SWRMutateConfig<DATA>.() -> Unit = {},
    ): Result<DATA?> {
        val currentConfig = SWRMutateConfig<DATA>().apply(config)
        val previousData = get(GettingFrom.LocalOnly).getOrNull()
        val optimisticData = currentConfig.optimisticData
        if (optimisticData != null) {
            update(optimisticData, false)
        }
        return runCatching {
            if (data != null) data() else null
        }.onSuccess { newData ->
            if (currentConfig.populateCache && newData != null) {
                update(newData, false)
            }
            if (currentConfig.revalidate) {
                validate()
            }
        }.onFailure {
            if (currentConfig.rollbackOnError) {
                update(previousData, true)
            }
        }
    }
}
