package com.kazakago.swr.compose.internal

import com.kazakago.swr.compose.SWRState
import com.kazakago.swr.runtime.SWRMutate
import com.kazakago.swr.store.SWRStoreState

internal fun <DATA> SWRStoreState<DATA>.toSWRState(
    mutate: SWRMutate<DATA>,
): SWRState<DATA> {
    return when (this) {
        is SWRStoreState.Loading -> SWRState.Loading(
            data = data,
            mutate = mutate,
        )

        is SWRStoreState.Completed -> SWRState.Completed(
            data = data,
            mutate = mutate,
        )

        is SWRStoreState.Error -> SWRState.Error(
            data = data,
            error = error,
            mutate = mutate,
        )
    }
}
