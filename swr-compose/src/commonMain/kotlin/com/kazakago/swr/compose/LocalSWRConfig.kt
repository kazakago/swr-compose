package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.defaultSWRConfig

/**
 * CompositionLocal that provides the current [SWRConfig].
 *
 * Defaults to [defaultSWRConfig]. Override with the [SWRConfig] composable to apply
 * scoped configuration to a subtree of composables.
 */
public val LocalSWRConfig: ProvidableCompositionLocal<SWRConfig<Any, Any>> = compositionLocalOf {
    defaultSWRConfig
}

/**
 * Applies scoped SWR configuration to all SWR composables within [content].
 *
 * Configuration values are merged with the current [LocalSWRConfig], so only the properties
 * explicitly set in [config] are overridden. Equivalent to React SWR's `<SWRConfig>` provider.
 *
 * @param config Lambda to configure options on the current [SWRConfig].
 * @param content The composable subtree that inherits this configuration.
 * @see LocalSWRConfig
 */
@Composable
public fun SWRConfig(
    config: SWRConfig<Any, Any>.() -> Unit,
    content: @Composable () -> Unit,
) {
    val currentConfig = SWRConfig<Any, Any>(LocalSWRConfig.current)
    CompositionLocalProvider(LocalSWRConfig provides currentConfig.apply(config)) {
        content()
    }
}
