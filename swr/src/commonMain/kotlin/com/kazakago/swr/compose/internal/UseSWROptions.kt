package com.kazakago.swr.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.SWRConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Composable
internal fun <KEY, DATA> RevalidateOnMount(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    isValidating: State<Boolean>,
    validate: () -> Unit,
) {
    val cache = LocalSWRCache.current
    val revalidateOnMount = config.revalidateOnMount
    val fallback = config.fallback
    val fallbackData = config.fallbackData
    val revalidateIfStale = config.revalidateIfStale
    LaunchedEffect(key, cache, revalidateOnMount, fallback, fallbackData, revalidateIfStale) {
        val fixedRevalidateOnMount = revalidateOnMount ?: (fallbackData == null && fallback[key] == null)
        if (fixedRevalidateOnMount) {
            var mutableRevalidateIfStale = revalidateIfStale
            while (true) {
                if (isValidating.value) {
                    delay(100)
                    mutableRevalidateIfStale = false
                    continue
                }
                val data = cache.state<KEY?, DATA>(key).value
                if (mutableRevalidateIfStale || data == null) {
                    validate()
                }
                break
            }
        }
    }
}

@Composable
internal fun <KEY, DATA> RevalidateOnFocus(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val systemCache = LocalSWRSystemCache.current
    val revalidateOnFocus = config.revalidateOnFocus
    val focusThrottleInterval = config.focusThrottleInterval
    LaunchedEffect(key, lifecycleOwner, systemCache, revalidateOnFocus, focusThrottleInterval) {
        if (revalidateOnFocus) {
            var isFirstTime = true
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val focusedTimerJob = systemCache.getFocusedTimerJob(key)
                if (!isFirstTime) {
                    if (focusedTimerJob == null || !focusedTimerJob.isActive) {
                        val newFocusedTimerJob = SWRGlobalScope.launch { delay(focusThrottleInterval) }
                        systemCache.setFocusedTimerJob(key, newFocusedTimerJob)
                        validate()
                    }
                }
                isFirstTime = false
            }
        }
    }
}

@Composable
internal fun <KEY, DATA> RevalidateOnReconnect(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val revalidateOnReconnect = config.revalidateOnReconnect
    LaunchedEffect(key, lifecycleOwner, revalidateOnReconnect) {
        if (revalidateOnReconnect) {
            GlobalKonnection.observeHasConnection().collect { hasConnection ->
                if (hasConnection) {
                    validate()
                }
            }
        }
    }
}

@Composable
internal fun <KEY, DATA> RefreshInterval(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val refreshInterval = config.refreshInterval
    val refreshWhenHidden = config.refreshWhenHidden
    val refreshWhenOffline = config.refreshWhenOffline
    LaunchedEffect(key, refreshInterval, refreshWhenHidden, refreshWhenOffline) {
        if (refreshInterval > Duration.ZERO) {
            while (true) {
                delay(refreshInterval)
                if (refreshWhenHidden || lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    if (refreshWhenOffline || GlobalKonnection.isConnected()) {
                        validate()
                    }
                }
            }
        }
    }
}
