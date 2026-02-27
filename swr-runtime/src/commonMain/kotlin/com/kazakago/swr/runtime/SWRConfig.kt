package com.kazakago.swr.runtime

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** The process-wide default [SWRConfig] used when no custom configuration is provided. */
public val defaultSWRConfig: SWRConfig<Any, Any> = SWRConfig()

/**
 * Configuration options that control SWR data-fetching behavior.
 *
 * These options mirror [React SWR's options](https://swr.vercel.app/docs/api#options).
 * Apply globally via [LocalSWRConfig][com.kazakago.swr.compose.LocalSWRConfig],
 * or per-hook via the trailing `config` lambda.
 *
 * Options prefixed with `initial` / `revalidateAll` / `revalidateFirstPage` / `persistSize` / `parallel`
 * apply only to [SWRInfinite] / [rememberSWRInfinite][com.kazakago.swr.compose.rememberSWRInfinite].
 */
public class SWRConfig<KEY : Any, DATA>(
    /** Revalidate data even when cached (stale) data already exists. Default: `true`. */
    public var revalidateIfStale: Boolean = true,

    /**
     * Revalidate when the composable mounts.
     * `null` inherits the value of [revalidateIfStale]. Default: `null`.
     */
    public var revalidateOnMount: Boolean? = null,

    /** Revalidate when the app regains focus. Default: `true`. */
    public var revalidateOnFocus: Boolean = true,

    /** Revalidate when the network reconnects. Default: `true`. */
    public var revalidateOnReconnect: Boolean = true,

    /** Polling interval. Set to `Duration.ZERO` to disable polling. Default: `0.seconds`. */
    public var refreshInterval: Duration = 0.seconds,

    /** Continue polling even when the app is in the background. Default: `false`. */
    public var refreshWhenHidden: Boolean = false,

    /** Continue polling even when the device is offline. Default: `false`. */
    public var refreshWhenOffline: Boolean = false,

    /** Automatically retry when a fetch fails. Default: `true`. */
    public var shouldRetryOnError: Boolean = true,

    /** Deduplicate requests with the same key within this interval. Default: `2.seconds`. */
    public var dedupingInterval: Duration = 2.seconds,

    /** Throttle focus-triggered revalidations within this interval. Default: `5.seconds`. */
    public var focusThrottleInterval: Duration = 5.seconds,

    /** Duration after which [onLoadingSlow] is called if the fetch has not completed. Default: `3.seconds`. */
    public var loadingTimeout: Duration = 3.seconds,

    /** Base interval for the exponential backoff retry strategy. Default: `5.seconds`. */
    public var errorRetryInterval: Duration = 5.seconds,

    /** Maximum number of retry attempts. `null` means unlimited. Default: `null`. */
    public var errorRetryCount: Int? = null,

    /** Data to return before any fetch completes, as a placeholder. Default: `null`. */
    public var fallbackData: DATA? = null,

    /** Called when a fetch takes longer than [loadingTimeout] to complete. */
    public var onLoadingSlow: ((key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)? = null,

    /** Called after data is successfully fetched. */
    public var onSuccess: ((data: DATA, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)? = null,

    /** Called when a fetch fails. */
    public var onError: ((error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)? = null,

    /**
     * Custom error retry handler.
     * Defaults to [OnErrorRetryDefault], which implements exponential backoff.
     */
    public var onErrorRetry: suspend (error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>, revalidate: suspend (option: SWRValidateOptions) -> Unit, options: SWRValidateOptions) -> Unit = OnErrorRetryDefault,

    // SWRInfinite-specific options

    /** Initial number of pages to load. Default: `1`. */
    public var initialSize: Int = 1,

    /** Revalidate all pages on focus or reconnect. Default: `false`. */
    public var revalidateAll: Boolean = false,

    /** Revalidate only the first page on focus or reconnect. Default: `true`. */
    public var revalidateFirstPage: Boolean = true,

    /** Preserve the current page count when the key changes. Default: `false`. */
    public var persistSize: Boolean = false,

    /** Retain the previous data while new data for a changed key is loading. Default: `false`. */
    public var keepPreviousData: Boolean = false,

    /** Fetch all pages in parallel instead of sequentially. Default: `false`. */
    public var parallel: Boolean = false,

    /**
     * Suspend all revalidations while this lambda returns `true`.
     * Throws [PausedException] internally when paused. Default: `null` (never paused).
     */
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
