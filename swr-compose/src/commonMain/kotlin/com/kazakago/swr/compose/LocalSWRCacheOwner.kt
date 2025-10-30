package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner

public val LocalSWRCacheOwner: ProvidableCompositionLocal<SWRCacheOwner> = compositionLocalOf {
    defaultSWRCacheOwner
}

@Composable
public fun SWRCacheOwner(
    cacheOwner: SWRCacheOwner,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSWRCacheOwner provides cacheOwner) {
        content()
    }
}
