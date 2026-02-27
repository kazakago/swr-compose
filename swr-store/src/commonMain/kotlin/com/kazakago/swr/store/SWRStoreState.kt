package com.kazakago.swr.store

/**
 * Represents the state of a single SWR cache entry.
 *
 * This is the core state type propagated through all SWR layers.
 * Use [isValidating], [data], and [error] for exhaustive state handling.
 */
public sealed interface SWRStoreState<out T> {

    public companion object {
        /** Returns the initial state used before any fetch has been started. */
        public fun <T> initialize(): SWRStoreState<T> = Loading(null)
    }

    /** The currently cached data, or `null` if not yet available. */
    public val data: T?

    /** The error from the last failed fetch, or `null` if no error occurred. */
    public val error: Throwable?

    /** `true` while a fetch or revalidation is in progress. */
    public val isValidating: Boolean

    public operator fun component1(): T? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isValidating

    /**
     * A fetch or revalidation is in progress.
     * [data] may be non-null when this represents a background refresh of stale data.
     */
    public data class Loading<out T>(
        override val data: T?,
    ) : SWRStoreState<T> {
        override val error: Throwable? = null
        override val isValidating: Boolean = true
    }

    /**
     * Data was fetched successfully and no revalidation is currently in progress.
     */
    public data class Completed<out T>(
        override val data: T,
    ) : SWRStoreState<T> {
        override val error: Throwable? = null
        override val isValidating: Boolean = false
    }

    /**
     * The last fetch attempt failed.
     * [data] may retain a previously cached value if available.
     */
    public data class Error<out T>(
        override val data: T?,
        override val error: Throwable,
    ) : SWRStoreState<T> {
        override val isValidating: Boolean = false
    }
}
