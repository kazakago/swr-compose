package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRMutate

/**
 * Compose state returned by [rememberSWR] and [rememberSWRImmutable].
 *
 * Equivalent to the return value of React SWR's `useSWR`.
 * Use the subclass type or [isLoading] / [isValidating] / [error] for exhaustive state handling.
 */
@Immutable
public sealed class SWRState<DATA> {

    public companion object {
        /** Returns the initial placeholder state used before the first emission. */
        public fun <DATA> initialize(): SWRState<DATA> = Loading(
            data = null,
            mutate = SWRMutate(
                get = { Result.failure(NoSuchElementException()) },
                validate = { Result.failure(NoSuchElementException()) },
                update = { _, _ -> },
            ),
        )
    }

    /** The currently cached data, or `null` if not yet available. */
    public abstract val data: DATA?

    /** The error from the last failed fetch, or `null` if no error occurred. */
    public abstract val error: Throwable?

    /** `true` while a fetch or revalidation is in progress. */
    public abstract val isValidating: Boolean

    /** `true` when validating and no data is available yet. Shorthand for `isValidating && data == null`. */
    public val isLoading: Boolean = isValidating && data == null

    /** Handle for programmatically mutating the cache entry for this key. */
    public abstract val mutate: SWRMutate<DATA>

    public operator fun component1(): DATA? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isValidating
    public operator fun component4(): Boolean = isLoading
    public operator fun component5(): SWRMutate<DATA> = mutate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SWRState<DATA>) return false
        if (this::class != other::class) return false

        if (data != other.data) return false
        if (error != other.error) return false
        if (isValidating != other.isValidating) return false
        if (mutate != other.mutate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = this::class.hashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + isValidating.hashCode()
        result = 31 * result + mutate.hashCode()
        return result
    }

    /**
     * A fetch or revalidation is in progress.
     * [data] may be non-null when this represents a background refresh of stale data.
     */
    public class Loading<DATA>(
        override val data: DATA?,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA>() {
        override val error: Throwable? = null
        override val isValidating: Boolean = true
    }

    /** Data was fetched successfully and no revalidation is currently in progress. */
    public class Completed<DATA>(
        override val data: DATA,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA>() {
        override val error: Throwable? = null
        override val isValidating: Boolean = false
    }

    /**
     * The last fetch attempt failed.
     * [data] may retain a previously cached value if available.
     */
    public class Error<DATA>(
        override val data: DATA?,
        override val error: Throwable,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA>() {
        override val isValidating: Boolean = false
    }
}
