package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRMutate

@Immutable
public sealed interface SWRState<DATA> {

    public companion object {
        public fun <DATA> initialize(): SWRState<DATA> = Loading(
            data = null,
            mutate = SWRMutate(
                get = { Result.failure(NoSuchElementException()) },
                validate = { Result.failure(NoSuchElementException()) },
                update = {},
            ),
        )
    }

    public val data: DATA?
    public val error: Throwable?
    public val isLoading: Boolean
    public val mutate: SWRMutate<DATA>

    public operator fun component1(): DATA? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isLoading
    public operator fun component4(): SWRMutate<DATA> = mutate

    public class Loading<DATA>(
        override val data: DATA?,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA> {
        override val error: Throwable? = null
        override val isLoading: Boolean = true
    }

    public class Completed<DATA>(
        override val data: DATA,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA> {
        override val error: Throwable? = null
        override val isLoading: Boolean = false
    }

    public class Error<DATA>(
        override val data: DATA?,
        override val error: Throwable,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA> {
        override val isLoading: Boolean = false
    }
}
