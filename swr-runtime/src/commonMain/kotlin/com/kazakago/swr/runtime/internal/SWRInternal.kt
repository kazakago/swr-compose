package com.kazakago.swr.runtime.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.kazakago.swr.runtime.DedupingIntervalException
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRValidateOptions
import com.kazakago.swr.store.GettingFrom
import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.SWRStoreState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class SWRInternal<KEY : Any, DATA>(
    val store: SWRStore<KEY, DATA>,
    lifecycleOwner: LifecycleOwner,
    private val scope: CoroutineScope,
    private val config: SWRConfig<KEY, DATA>,
) {
    private val networkMonitor = buildNetworkMonitor()
    private val retryingJobs = mutableSetOf<Job>()
    private var dedupingIntervalJob: Job? = null

    init {
        when (config.revalidateOnMount) {
            true -> scope.launch { validate() }
            false -> {}
            else -> scope.launch {
                val data = store.get(GettingFrom.LocalOnly)
                if (config.revalidateIfStale || data.isFailure) {
                    validate()
                }
            }
        }
        if (config.revalidateOnFocus) {
            scope.launch {
                var stateThrottleIntervalJob = scope.launch { delay(2.seconds) }
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    if (!stateThrottleIntervalJob.isActive) {
                        stateThrottleIntervalJob = scope.launch {
                            validate()
                            delay(config.focusThrottleInterval)
                        }
                    }
                }
            }
        }
        if (config.revalidateOnReconnect) {
            scope.launch {
                networkMonitor.onlineStatusFlow.collect { isOnline ->
                    if (isOnline && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        validate()
                    }
                }
            }
        }
        if (config.refreshInterval > Duration.ZERO) {
            scope.launch {
                val validateInterval = suspend {
                    while (isActive) {
                        delay(config.refreshInterval)
                        if (networkMonitor.isOnline || config.refreshWhenOffline) {
                            validate()
                        }
                    }
                }
                if (config.refreshWhenHidden) {
                    validateInterval()
                } else {
                    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        validateInterval()
                    }
                }
            }
        }
    }

    suspend fun validate(options: SWRValidateOptions? = null): Result<DATA> {
        if (dedupingIntervalJob?.isActive == true) {
            return Result.failure(DedupingIntervalException())
        }
        dedupingIntervalJob = scope.launch {
            delay(config.dedupingInterval)
        }
        val loadingTimeoutJob = scope.launch {
            delay(config.loadingTimeout)
            config.onLoadingSlow?.invoke()
        }
        return store.validate()
            .onSuccess { data ->
                loadingTimeoutJob.cancel()
                retryingJobs.clear()
                config.onSuccess?.invoke(data, store.key, config)
            }
            .onFailure { error ->
                loadingTimeoutJob.cancel()
                config.onError?.invoke(error, store.key, config)
                if (config.shouldRetryOnError) {
                    val revalidateOptions = createValidateOptions(options?.retryCount ?: 0)
                    retryingJobs += scope.launch {
                        config.onErrorRetry(
                            error,
                            store.key,
                            config,
                            ::validate,
                            revalidateOptions,
                        )
                    }
                }
            }
    }

    private fun createValidateOptions(currentRetryCount: Int): SWRValidateOptions {
        return SWRValidateOptions(
            retryCount = currentRetryCount + 1,
            dedupe = (0 == currentRetryCount) && retryingJobs.any { it.isActive },
        )
    }

    val stateFlow: StateFlow<SWRStoreState<DATA>> = store.flow
        .map { storeState ->
            val fallbackData = config.fallbackData
            if (storeState.data == null && fallbackData != null) {
                when (storeState) {
                    is SWRStoreState.Completed -> SWRStoreState.Completed(fallbackData)
                    is SWRStoreState.Error -> SWRStoreState.Error(fallbackData, storeState.error)
                    is SWRStoreState.Loading -> SWRStoreState.Loading(fallbackData)
                }
            } else {
                storeState
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = SWRStoreState.initialize(),
        )
}
