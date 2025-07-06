package com.kazakago.swr.store.internal

import com.kazakago.swr.store.GettingFrom
import com.kazakago.swr.store.SWRAlreadyLoadingException
import com.kazakago.swr.store.SWRStoreState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

internal class DataSelector<DATA>(
    private val getDataFromRemote: suspend () -> DATA,
    private val getDataFromLocal: suspend () -> DATA?,
    private val putDataToLocal: suspend (data: DATA?) -> Unit,
    private val getStateFlow: () -> Flow<DataState>,
    private val getState: () -> DataState,
    private val putState: (state: DataState) -> Unit,
) {

    val flow: Flow<SWRStoreState<DATA>> = getStateFlow()
        .map { state ->
            state.toStoreState(getDataFromLocal())
        }
        .distinctUntilChanged()

    suspend fun get(from: GettingFrom): Result<DATA> {
        when (from) {
            GettingFrom.Both -> if (getDataFromLocal() == null) validate()
            GettingFrom.RemoteOnly -> validate()
            GettingFrom.LocalOnly -> Unit
        }
        return runCatching {
            getStateFlow()
                .mapNotNull { state ->
                    val data = getDataFromLocal()
                    when (from) {
                        GettingFrom.Both -> {
                            when (state) {
                                is DataState.Fixed -> data ?: throw NoSuchElementException()
                                is DataState.Loading -> data
                                is DataState.Error -> data ?: throw state.error
                            }
                        }

                        GettingFrom.RemoteOnly -> {
                            when (state) {
                                is DataState.Fixed -> data ?: throw NoSuchElementException()
                                is DataState.Loading -> null
                                is DataState.Error -> throw state.error
                            }
                        }

                        GettingFrom.LocalOnly -> {
                            when (state) {
                                is DataState.Fixed -> data ?: throw NoSuchElementException()
                                is DataState.Loading -> data ?: throw NoSuchElementException()
                                is DataState.Error -> data ?: throw state.error
                            }
                        }
                    }
                }
                .first()
        }
    }

    suspend fun validate(): Result<DATA> {
        val state = getState()
        return if (state !is DataState.Loading) {
            runCatching {
                putState(DataState.Loading())
                getDataFromRemote()
            }.onSuccess { data ->
                putDataToLocal(data)
                putState(DataState.Fixed())
                data
            }.onFailure { error ->
                putState(DataState.Error(error))
            }
        } else {
            Result.failure(SWRAlreadyLoadingException())
        }
    }

    suspend fun refresh(): Result<DATA> {
        clear()
        return validate()
    }

    suspend fun update(data: DATA?) {
        putDataToLocal(data)
        putState(DataState.Fixed())
    }

    suspend fun clear() {
        update(null)
    }
}
