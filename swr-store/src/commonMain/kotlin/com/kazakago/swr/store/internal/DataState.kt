package com.kazakago.swr.store.internal

internal sealed interface DataState {

    companion object {
        fun initialize(): DataState = Fixed()
    }

    class Fixed : DataState
    class Loading : DataState
    class Error(val error: Throwable) : DataState
}
