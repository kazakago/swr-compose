package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRInfiniteMutate

@Immutable
public sealed interface SWRInfiniteState<DATA> {

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

    public val data: List<DATA?>?
    public val error: Throwable?
    public val isLoading: Boolean
    public val mutate: SWRInfiniteMutate<DATA>
    public val size: Int
    public val setSize: (size: Int) -> Unit

    public operator fun component1(): List<DATA?>? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isLoading
    public operator fun component4(): SWRInfiniteMutate<DATA> = mutate
    public operator fun component5(): Int = size
    public operator fun component6(): ((size: Int) -> Unit) = setSize

    public class Loading<DATA>(
        override val data: List<DATA?>?,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA> {
        override val error: Throwable? = null
        override val isLoading: Boolean = true
    }

    public class Completed<DATA>(
        override val data: List<DATA?>,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA> {
        override val error: Throwable? = null
        override val isLoading: Boolean = false
    }

    public class Error<DATA>(
        override val data: List<DATA?>?,
        override val error: Throwable,
        override val mutate: SWRInfiniteMutate<DATA>,
        override val size: Int,
        override val setSize: (Int) -> Unit,
    ) : SWRInfiniteState<DATA> {
        override val isLoading: Boolean = false
    }
}
