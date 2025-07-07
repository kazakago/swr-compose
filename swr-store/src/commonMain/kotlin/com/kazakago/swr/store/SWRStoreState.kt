package com.kazakago.swr.store

public sealed interface SWRStoreState<out T> {

    public companion object {
        public fun <T> initialize(): SWRStoreState<T> = Loading(null)
    }

    public val data: T?
    public val error: Throwable?
    public val isLoading: Boolean

    public operator fun component1(): T? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isLoading

    public data class Loading<out T>(
        override val data: T?,
    ) : SWRStoreState<T> {
        override val error: Throwable? = null
        override val isLoading: Boolean = true
    }

    public data class Completed<out T>(
        override val data: T,
    ) : SWRStoreState<T> {
        override val error: Throwable? = null
        override val isLoading: Boolean = false
    }

    public data class Error<out T>(
        override val data: T?,
        override val error: Throwable,
    ) : SWRStoreState<T> {
        override val isLoading: Boolean = false
    }
}
