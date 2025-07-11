package com.kazakago.swr.runtime

import com.kazakago.swr.runtime.internal.SWRValidate
import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope

public class SWRPreload<KEY : Any, DATA>(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>? = null,
    cacheOwner: SWRCacheOwner = defaultSWRCacheOwner,
    defaultConfig: SWRConfig<Any, Any> = defaultSWRConfig,
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
) {
    private val validate: SWRValidate<KEY, DATA>? = if (key != null) {
        SWRValidate(
            store = SWRStore(key, fetcher, persister, cacheOwner),
            scope = scope,
            config = SWRConfig<KEY, DATA>(defaultConfig).apply(config),
        )
    } else {
        null
    }

    public suspend operator fun invoke() {
        validate?.invoke()
    }
}
