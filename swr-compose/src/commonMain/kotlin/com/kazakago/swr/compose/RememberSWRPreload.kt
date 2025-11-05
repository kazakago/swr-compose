package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRPreload
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope

@Composable
public fun <KEY : Any, DATA> rememberSWRPreload(
    key: KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWRPreload<KEY, DATA> {
    val cacheOwner = LocalSWRCacheOwner.current
    val defaultConfig = LocalSWRConfig.current
    return retain(key) {
        SWRPreload(
            key = key,
            fetcher = fetcher,
            scope = scope,
            persister = persister,
            cacheOwner = cacheOwner,
            defaultConfig = defaultConfig,
            config = config,
        )
    }
}
