package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRMutate

@Immutable
public sealed class SWRState<DATA> {

    public companion object {
        public fun <DATA> initialize(): SWRState<DATA> = Loading(
            data = null,
            mutate = SWRMutate(
                get = { Result.failure(NoSuchElementException()) },
                validate = { Result.failure(NoSuchElementException()) },
                update = { _, _ -> },
            ),
        )
    }

    public abstract val data: DATA?
    public abstract val error: Throwable?
    public abstract val isLoading: Boolean
    public abstract val mutate: SWRMutate<DATA>

    public operator fun component1(): DATA? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isLoading
    public operator fun component4(): SWRMutate<DATA> = mutate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SWRState<DATA>) return false
        if (this::class != other::class) return false

        if (data != other.data) return false
        if (error != other.error) return false
        if (isLoading != other.isLoading) return false
        if (mutate != other.mutate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = this::class.hashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + mutate.hashCode()
        return result
    }

    public class Loading<DATA>(
        override val data: DATA?,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA>() {
        override val error: Throwable? = null
        override val isLoading: Boolean = true
    }

    public class Completed<DATA>(
        override val data: DATA,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA>() {
        override val error: Throwable? = null
        override val isLoading: Boolean = false
    }

    public class Error<DATA>(
        override val data: DATA?,
        override val error: Throwable,
        override val mutate: SWRMutate<DATA>,
    ) : SWRState<DATA>() {
        override val isLoading: Boolean = false
    }
}
