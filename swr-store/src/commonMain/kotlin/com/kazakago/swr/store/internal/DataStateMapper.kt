package com.kazakago.swr.store.internal

import com.kazakago.swr.store.SWRStoreState

internal fun <DATA> DataState.toStoreState(content: DATA?): SWRStoreState<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            SWRStoreState.Completed(content)
        } else {
            SWRStoreState.initialize()
        }

        is DataState.Loading -> SWRStoreState.Loading(content)
        is DataState.Error -> SWRStoreState.Error(content, error)
    }
}
