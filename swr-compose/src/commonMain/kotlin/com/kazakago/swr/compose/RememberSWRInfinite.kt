package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazakago.swr.compose.internal.swrInfiniteSaver
import com.kazakago.swr.compose.internal.toSWRInfiniteState
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRInfinite
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map

@Composable
public fun <KEY : Any, DATA> rememberSWRInfinite(
    getKey: (pageIndex: Int, previousPageData: DATA?) -> KEY?,
    fetcher: suspend (key: KEY) -> DATA,
    persister: Persister<KEY, DATA>? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    config: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWRInfiniteState<DATA> {
    return if (!LocalInspectionMode.current) {
        val cacheOwner = LocalSWRCacheOwner.current
        val defaultConfig = LocalSWRConfig.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val swr = rememberSaveable(
            saver = swrInfiniteSaver(
                getKey = getKey,
                fetcher = fetcher,
                lifecycleOwner = lifecycleOwner,
                scope = scope,
                persister = persister,
                cacheOwner = cacheOwner,
                defaultConfig = defaultConfig,
                config = config,
            )
        ) {
            SWRInfinite(
                getKey = getKey,
                fetcher = fetcher,
                lifecycleOwner = lifecycleOwner,
                scope = scope,
                persister = persister,
                cacheOwner = cacheOwner,
                defaultConfig = defaultConfig,
                config = config,
            )
        }
        swr.stateFlow
            .map { it.toSWRInfiniteState(swr.mutate, swr.getSize(), swr::setSize) }
            .collectAsStateWithLifecycle(SWRInfiniteState.initialize())
            .value
    } else {
        SWRInfiniteState.initialize()
    }
}
