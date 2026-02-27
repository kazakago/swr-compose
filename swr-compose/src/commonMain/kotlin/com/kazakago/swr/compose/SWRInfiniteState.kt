package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRInfiniteMutate

/**
 * Compose state returned by [rememberSWRInfinite].
 *
 * Equivalent to the return value of React SWR's `useSWRInfinite`.
 * Load more pages by incrementing [size] via [setSize].
 */
@Immutable
public sealed class SWRInfiniteState<DATA> {

    public companion object {
        /** Returns the initial placeholder state used before the first emission. */
        public fun <DATA> initialize(): SWRInfiniteState<DATA> = Loading(
            data = null,
            size = 1,
            setSize = {},
            mutate = SWRInfiniteMutate(
                getSize = { 1 },
                get = { Result.failure(NoSuchElementException()) },
                validate = { Result.failure(NoSuchElementException()) },
                update = { _, _ -> },
            ),
        )
    }

    /** List of pages, where each element is the data for one page (or `null` if not yet loaded). */
    public abstract val data: List<DATA?>?

    /** The error from the last failed page fetch, or `null` if no error occurred. */
    public abstract val error: Throwable?

    /** `true` while any page fetch or revalidation is in progress. */
    public abstract val isValidating: Boolean

    /** `true` when validating and no data is available yet. Shorthand for `isValidating && data == null`. */
    public val isLoading: Boolean = isValidating && data == null

    /** Handle for programmatically mutating the cache across all loaded pages. */
    public abstract val mutate: SWRInfiniteMutate<DATA>

    /** The current number of loaded pages. */
    public abstract val size: Int

    /** Call with a new page count to load more (or fewer) pages. */
    public abstract val setSize: (size: Int) -> Unit

    public operator fun component1(): List<DATA?>? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isValidating
    public operator fun component4(): Boolean = isLoading
    public operator fun component5(): SWRInfiniteMutate<DATA> = mutate
    public operator fun component6(): Int = size
    public operator fun component7(): ((size: Int) -> Unit) = setSize

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SWRInfiniteState<*>

        if (data != other.data) return false
        if (error != other.error) return false
        if (isValidating != other.isValidating) return false
        if (mutate != other.mutate) return false
        if (size != other.size) return false
        if (setSize != other.setSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = this::class.hashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + isValidating.hashCode()
        result = 31 * result + mutate.hashCode()
        result = 31 * result + size
        result = 31 * result + setSize.hashCode()
        return result
    }

    /**
     * A page fetch or revalidation is in progress.
     * [data] may be non-null when this represents a background refresh of existing pages.
     */
    public class Loading<DATA>(
        override val data: List<DATA?>?,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA>() {
        override val error: Throwable? = null
        override val isValidating: Boolean = true
    }

    /** All pages were fetched successfully and no revalidation is currently in progress. */
    public class Completed<DATA>(
        override val data: List<DATA?>,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA>() {
        override val error: Throwable? = null
        override val isValidating: Boolean = false
    }

    /**
     * At least one page fetch failed.
     * [data] may retain previously cached page data if available.
     */
    public class Error<DATA>(
        override val data: List<DATA?>?,
        override val error: Throwable,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA>() {
        override val isValidating: Boolean = false
    }
}
