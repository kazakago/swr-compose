package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.kazakago.swr.store.cache.SWRCacheOwner
import com.kazakago.swr.store.cache.defaultSWRCacheOwner

/**
 * CompositionLocal that provides the current [SWRCacheOwner].
 *
 * Defaults to [defaultSWRCacheOwner]. Override with the [SWRCacheOwner] composable to isolate
 * the SWR cache namespace (e.g. per user session or feature scope).
 */
public val LocalSWRCacheOwner: ProvidableCompositionLocal<SWRCacheOwner> = compositionLocalOf {
    defaultSWRCacheOwner
}

/**
 * Overrides the [SWRCacheOwner] for all SWR composables within [content].
 *
 * Use this to isolate the cache namespace, for example when switching user accounts
 * or scoping data to a particular feature.
 *
 * @param cacheOwner The [SWRCacheOwner] to use within [content].
 * @param content The composable subtree that uses this cache owner.
 * @see LocalSWRCacheOwner
 */
@Composable
public fun SWRCacheOwner(
    cacheOwner: SWRCacheOwner,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSWRCacheOwner provides cacheOwner) {
        content()
    }
}
