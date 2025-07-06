package com.kazakago.swr.runtime

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

public data class SWRInfiniteMutate<DATA>(
    private val getSize: suspend () -> Int,
    private val get: suspend (pageIndex: Int) -> Result<DATA?>,
    private val validate: suspend (pageIndex: Int) -> Result<DATA>,
    private val update: suspend (pageIndex: Int, newData: DATA?) -> Unit,
) {

    public suspend operator fun invoke(
        data: (suspend () -> List<DATA>)? = null,
        config: SWRMutateConfig<List<DATA>>.() -> Unit = {},
    ): Result<List<DATA>?> {
        val currentConfig = SWRMutateConfig<List<DATA>>().apply(config)
        val pageSize = getSize()
        val previousList = coroutineScope {
            buildList {
                repeat(pageSize) { pageIndex ->
                    add(async { this@SWRInfiniteMutate.get(pageIndex).getOrNull() })
                }
            }.awaitAll()
        }
        val optimisticList = currentConfig.optimisticData
        if (optimisticList != null) {
            coroutineScope {
                buildList {
                    repeat(pageSize) { pageIndex ->
                        runCatching {
                            optimisticList[pageIndex]
                        }.onSuccess { optimisticData ->
                            add(async { update(pageIndex, optimisticData) })
                        }
                    }
                }.awaitAll()
            }
        }
        return runCatching {
            if (data != null) data() else null
        }.onSuccess { newDataList ->
            if (newDataList != null) {
                coroutineScope {
                    buildList {
                        repeat(pageSize) { pageIndex ->
                            runCatching {
                                newDataList[pageIndex]
                            }.onSuccess { newData ->
                                add(async { update(pageIndex, newData) })
                            }
                        }
                    }.awaitAll()
                }
            }
            if (currentConfig.revalidate) {
                coroutineScope {
                    buildList {
                        repeat(pageSize) { pageIndex ->
                            add(async { validate(pageIndex) })
                        }
                    }.awaitAll()
                }
            }
        }.onFailure { throwable ->
            if (currentConfig.rollbackOnError) {
                coroutineScope {
                    buildList {
                        repeat(pageSize) { pageIndex ->
                            runCatching {
                                previousList[pageIndex]
                            }.onSuccess { previousData ->
                                add(async { update(pageIndex, previousData) })
                            }
                        }
                    }.awaitAll()
                }
            }
        }
    }
}
