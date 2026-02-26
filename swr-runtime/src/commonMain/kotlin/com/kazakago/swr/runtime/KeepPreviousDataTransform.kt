package com.kazakago.swr.runtime

import com.kazakago.swr.store.SWRStoreState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class KeepPreviousDataHolder<DATA> {
    internal var previousData: DATA? = null
}

public fun <DATA> Flow<SWRStoreState<DATA>>.withKeepPreviousData(
    holder: KeepPreviousDataHolder<DATA>,
): Flow<SWRStoreState<DATA>> = map { state ->
    val effectiveState = if (state.data == null && holder.previousData != null) {
        when (state) {
            is SWRStoreState.Loading -> SWRStoreState.Loading(holder.previousData)
            is SWRStoreState.Completed -> state
            is SWRStoreState.Error -> SWRStoreState.Error(holder.previousData, state.error)
        }
    } else {
        state
    }
    if (state.data != null) {
        holder.previousData = state.data
    }
    effectiveState
}
