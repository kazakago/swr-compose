package com.kazakago.swr.compose.internal

import androidx.compose.runtime.saveable.mapSaver
import androidx.lifecycle.LifecycleOwner
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRInfinite
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope

@Suppress("UNCHECKED_CAST")
internal fun <KEY : Any, DATA> swrInfiniteSaver(
    getKey: (pageIndex: Int, previousPageData: DATA?) -> KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    persister: Persister<KEY, DATA>?,
    cacheOwner: SWRCacheOwner,
    defaultConfig: SWRConfig<Any, Any>,
    config: SWRConfig<KEY, DATA>.() -> Unit,
) = mapSaver(
    save = {
        mapOf(
            "previousFirstKey" to it.previousFirstKey,
            "pageSize" to it.pageSize.value,
        )
    },
    restore = {
        SWRInfinite(
            getKey = getKey,
            fetcher = fetcher,
            lifecycleOwner = lifecycleOwner,
            scope = scope,
            persister = persister,
            cacheOwner = cacheOwner,
            defaultConfig = defaultConfig,
            config = config,
        ).apply {
            previousFirstKey = it["previousFirstKey"] as KEY?
            pageSize.value = it["pageSize"] as Int
        }
    }
)
