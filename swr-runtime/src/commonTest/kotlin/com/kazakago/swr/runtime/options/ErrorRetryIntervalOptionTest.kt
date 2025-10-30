package com.kazakago.swr.runtime.options

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.store.cache.SWRCacheOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorRetryIntervalOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun errorRetryIntervalOption5seconds() = runTest {
        val errorRetryIntervalList = mutableListOf<Duration>()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw IllegalStateException()
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            errorRetryInterval = 5.seconds
            onErrorRetry = { _, _, config, _, _ ->
                errorRetryIntervalList += config.errorRetryInterval
            }
        }
        swr.stateFlow.test {
            advanceTimeBy(1000)
            skipItems(2)
            assertEquals(listOf(5.seconds), errorRetryIntervalList)
        }
    }

    @Test
    fun errorRetryIntervalOption10seconds() = runTest {
        val errorRetryIntervalList = mutableListOf<Duration>()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw IllegalStateException()
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            errorRetryInterval = 10.seconds
            onErrorRetry = { _, _, config, _, _ ->
                errorRetryIntervalList += config.errorRetryInterval
            }
        }
        swr.stateFlow.test {
            advanceTimeBy(1000)
            skipItems(2)
            assertEquals(listOf(10.seconds), errorRetryIntervalList)
        }
    }
}
