package com.kazakago.swr.runtime

import com.kazakago.swr.store.GettingFrom

public data class SWRMutate<DATA>(
    private val get: suspend (from: GettingFrom) -> Result<DATA>,
    private val validate: suspend () -> Result<DATA>,
    private val update: suspend (newData: DATA?) -> Unit,
) {

    public suspend operator fun invoke(
        data: (suspend () -> DATA)? = null,
        config: SWRMutateConfig<DATA>.() -> Unit = {},
    ): Result<DATA?> {
        val currentConfig = SWRMutateConfig<DATA>().apply(config)
        val previousData = get(GettingFrom.LocalOnly).getOrNull()
        val optimisticData = currentConfig.optimisticData
        if (optimisticData != null) {
            update(optimisticData)
        }
        return runCatching {
            if (data != null) data() else null
        }.onSuccess { newData ->
            if (newData != null) {
                update(newData)
            }
            if (currentConfig.revalidate) {
                validate()
            }
        }.onFailure { throwable ->
            if (currentConfig.rollbackOnError) {
                update(previousData)
            }
        }
    }
}
