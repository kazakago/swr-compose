package com.kazakago.swr.runtime

public data class SWRMutateConfig<DATA>(
    var optimisticData: DATA? = null,
    var revalidate: Boolean = true,
    var populateCache: Boolean = true,
    var rollbackOnError: Boolean = true,
)
