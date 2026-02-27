package com.kazakago.swr.runtime

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public val defaultSWRConfig: SWRConfig<Any, Any> = SWRConfig()

public class SWRConfig<KEY : Any, DATA>(
    public var revalidateIfStale: Boolean = true,
    public var revalidateOnMount: Boolean? = null,
    public var revalidateOnFocus: Boolean = true,
    public var revalidateOnReconnect: Boolean = true,
    public var refreshInterval: Duration = 0.seconds,
    public var refreshWhenHidden: Boolean = false,
    public var refreshWhenOffline: Boolean = false,
    public var shouldRetryOnError: Boolean = true,
    public var dedupingInterval: Duration = 2.seconds,
    public var focusThrottleInterval: Duration = 5.seconds,
    public var loadingTimeout: Duration = 3.seconds,
    public var errorRetryInterval: Duration = 5.seconds,
    public var errorRetryCount: Int? = null,
    public var fallbackData: DATA? = null,
    public var onLoadingSlow: ((key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)? = null,
    public var onSuccess: ((data: DATA, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)? = null,
    public var onError: ((error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)? = null,
    public var onErrorRetry: suspend (error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>, revalidate: suspend (option: SWRValidateOptions) -> Unit, options: SWRValidateOptions) -> Unit = OnErrorRetryDefault,

    public var initialSize: Int = 1,
    public var revalidateAll: Boolean = false,
    public var revalidateFirstPage: Boolean = true,
    public var persistSize: Boolean = false,
    public var keepPreviousData: Boolean = false,
    public var parallel: Boolean = false,
    public var isPaused: (() -> Boolean)? = null,
) {
    @Suppress("UNCHECKED_CAST")
    public constructor(config: SWRConfig<Any, Any>) : this(
        revalidateIfStale = config.revalidateIfStale,
        revalidateOnMount = config.revalidateOnMount,
        revalidateOnFocus = config.revalidateOnFocus,
        revalidateOnReconnect = config.revalidateOnReconnect,
        refreshInterval = config.refreshInterval,
        refreshWhenHidden = config.refreshWhenHidden,
        refreshWhenOffline = config.refreshWhenOffline,
        shouldRetryOnError = config.shouldRetryOnError,
        dedupingInterval = config.dedupingInterval,
        focusThrottleInterval = config.focusThrottleInterval,
        loadingTimeout = config.loadingTimeout,
        errorRetryInterval = config.errorRetryInterval,
        errorRetryCount = config.errorRetryCount,
        fallbackData = null,
        onLoadingSlow = config.onLoadingSlow as ((key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
        onSuccess = config.onSuccess as ((data: DATA, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
        onError = config.onError as ((error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
        onErrorRetry = config.onErrorRetry as suspend (error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>, revalidate: suspend (SWRValidateOptions) -> Unit, options: SWRValidateOptions) -> Unit,
        initialSize = config.initialSize,
        revalidateAll = config.revalidateAll,
        revalidateFirstPage = config.revalidateFirstPage,
        persistSize = config.persistSize,
        keepPreviousData = config.keepPreviousData,
        parallel = config.parallel,
        isPaused = config.isPaused,
    )
}
