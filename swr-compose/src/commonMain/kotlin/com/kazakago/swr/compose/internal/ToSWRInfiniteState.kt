package com.kazakago.swr.compose.internal

import com.kazakago.swr.compose.SWRInfiniteState
import com.kazakago.swr.runtime.SWRInfiniteMutate
import com.kazakago.swr.store.SWRStoreState

internal fun <DATA> SWRStoreState<List<DATA?>>.toSWRInfiniteState(
    mutate: SWRInfiniteMutate<DATA>,
    size: Int,
    setSize: (Int) -> Unit,
): SWRInfiniteState<DATA> {
    return when (this) {
        is SWRStoreState.Loading -> SWRInfiniteState.Loading(
            data = data,
            mutate = mutate,
            size = size,
            setSize = setSize,
        )

        is SWRStoreState.Completed -> SWRInfiniteState.Completed(
            data = data,
            mutate = mutate,
            size = size,
            setSize = setSize,
        )

        is SWRStoreState.Error -> SWRInfiniteState.Error(
            data = data,
            error = error,
            mutate = mutate,
            size = size,
            setSize = setSize,
        )
    }
}
