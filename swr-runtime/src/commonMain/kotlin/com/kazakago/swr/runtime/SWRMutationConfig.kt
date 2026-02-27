package com.kazakago.swr.runtime

/**
 * Configuration for a single [SWRTrigger] call.
 *
 * Equivalent to React SWR's `SWRMutationConfiguration`.
 */
public class SWRMutationConfig<KEY : Any, DATA> {
    /** Optimistic data to display immediately while the mutation is in progress. */
    public var optimisticData: DATA? = null

    /** If `true`, revalidate from the remote source after the mutation completes. Default: `true`. */
    public var revalidate: Boolean = true

    /** If `true`, update the SWR cache with the value returned by the mutation. Default: `false`. */
    public var populateCache: Boolean = false

    /** If `true`, revert to the previous cached data when the mutation fails. Default: `true`. */
    public var rollbackOnError: Boolean = true

    /** Called when the mutation succeeds. */
    public var onSuccess: ((data: DATA, key: KEY, config: SWRMutationConfig<KEY, DATA>) -> Unit)? = null

    /** Called when the mutation fails. */
    public var onError: ((error: Throwable, key: KEY, config: SWRMutationConfig<KEY, DATA>) -> Unit)? = null
}
