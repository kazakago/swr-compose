package com.kazakago.swr.runtime

public class SWRMutationConfig<KEY : Any, DATA> {
    public var optimisticData: DATA? = null
    public var revalidate: Boolean = true
    public var populateCache: Boolean = false
    public var rollbackOnError: Boolean = true
    public var onSuccess: ((data: DATA, key: KEY, config: SWRMutationConfig<KEY, DATA>) -> Unit)? = null
    public var onError: ((error: Throwable, key: KEY, config: SWRMutationConfig<KEY, DATA>) -> Unit)? = null
}
