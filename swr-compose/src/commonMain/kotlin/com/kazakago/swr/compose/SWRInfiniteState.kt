package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRInfiniteMutate

@Immutable
public sealed class SWRInfiniteState<DATA> {

    public companion object {
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

    public abstract val data: List<DATA?>?
    public abstract val error: Throwable?
    public abstract val isValidating: Boolean
    public val isLoading: Boolean get() = isValidating && data == null
    public abstract val mutate: SWRInfiniteMutate<DATA>
    public abstract val size: Int
    public abstract val setSize: (size: Int) -> Unit

    public operator fun component1(): List<DATA?>? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isValidating
    public operator fun component4(): SWRInfiniteMutate<DATA> = mutate
    public operator fun component5(): Int = size
    public operator fun component6(): ((size: Int) -> Unit) = setSize

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

    public class Loading<DATA>(
        override val data: List<DATA?>?,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA>() {
        override val error: Throwable? = null
        override val isValidating: Boolean = true
    }

    public class Completed<DATA>(
        override val data: List<DATA?>,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA>() {
        override val error: Throwable? = null
        override val isValidating: Boolean = false
    }

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
