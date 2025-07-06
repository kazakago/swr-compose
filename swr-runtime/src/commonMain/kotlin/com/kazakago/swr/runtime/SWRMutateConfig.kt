package com.kazakago.swr.runtime

public data class SWRMutateConfig<DATA>(
    public var revalidate: Boolean = true,
    public var optimisticData: DATA? = null,
    public var rollbackOnError: Boolean = true,
)
