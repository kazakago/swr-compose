package com.kazakago.swr.runtime

import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.random.Random

public val OnErrorRetryDefault: suspend (
    error: Throwable,
    key: Any,
    config: SWRConfig<*, *>,
    revalidate: suspend (options: SWRValidateOptions) -> Unit,
    options: SWRValidateOptions,
) -> Unit = { _, _, config, revalidate, options ->
    if (!options.dedupe && config.errorRetryCount.let { it == null || options.retryCount <= it }) {
        val exponentialBackoff = floor((Random.nextDouble() + 0.5) * 1.shl(options.retryCount)).toLong() * config.errorRetryInterval.inWholeMilliseconds
        delay(exponentialBackoff)
        revalidate(options)
    }
}
