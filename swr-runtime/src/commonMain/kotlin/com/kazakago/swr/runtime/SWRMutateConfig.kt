package com.kazakago.swr.runtime

/**
 * Configuration for a single [SWRMutate] call.
 *
 * Equivalent to React SWR's `MutatorOptions`.
 */
public data class SWRMutateConfig<DATA>(
    /** Optimistic data to display immediately while the mutation is in progress. */
    var optimisticData: DATA? = null,

    /** If `true`, revalidate from the remote source after the mutation completes. Default: `true`. */
    var revalidate: Boolean = true,

    /** If `true`, update the cache with the value returned by the mutation. Default: `true`. */
    var populateCache: Boolean = true,

    /** If `true`, revert to the previous cached data when the mutation fails. Default: `true`. */
    var rollbackOnError: Boolean = true,
)
