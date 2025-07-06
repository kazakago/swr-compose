package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.defaultSWRConfig

public val LocalSWRConfig: ProvidableCompositionLocal<SWRConfig<Any, Any>> = compositionLocalOf {
    defaultSWRConfig
}

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
