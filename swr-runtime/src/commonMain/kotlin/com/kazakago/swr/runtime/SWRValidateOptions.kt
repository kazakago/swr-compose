package com.kazakago.swr.runtime

/**
 * Options passed to [SWRConfig.onErrorRetry] to control retry behavior.
 */
public data class SWRValidateOptions(
    /** The current retry attempt count (0-based). */
    public val retryCount: Int,

    /** Whether this revalidation is within the [dedupingInterval][SWRConfig.dedupingInterval] window. */
    public val dedupe: Boolean,
)
