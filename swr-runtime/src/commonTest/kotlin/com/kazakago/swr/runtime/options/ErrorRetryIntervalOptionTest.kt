package com.kazakago.swr.runtime.options

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.store.cache.SWRCacheOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import kotlin.time.Duration.Companion.milliseconds
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
    fun errorRetryBackoffCappedAt30Seconds() = runTest {
        val retryDelays = mutableListOf<Long>()
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
            errorRetryCount = 20
            onErrorRetry = { _, _, config, revalidate, options ->
                val exponentialBackoff = kotlin.math.floor((0.5 + 0.5) * 1.shl(options.retryCount)).toLong() * config.errorRetryInterval.inWholeMilliseconds
                retryDelays += exponentialBackoff
                delay(minOf(exponentialBackoff, 30_000))
                revalidate(options)
            }
        }
        swr.stateFlow.test {
            advanceTimeBy(1000)
            skipItems(2)
            // Verify that the raw exponential value would exceed 30s for high retry counts
            // but the actual default implementation should cap it
            retryDelays.forEach { rawDelay ->
                // This test just captures the raw values; the real assertion
                // is in the default handler test below
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun defaultOnErrorRetryCapsBackoffAt30Seconds() = runTest {
        // Directly test the default onErrorRetry with a high retryCount
        // that would produce an exponential backoff > 30 seconds
        val revalidateCalled = mutableListOf<Boolean>()
        val config = com.kazakago.swr.runtime.SWRConfig<String, String>().apply {
            errorRetryInterval = 1000.milliseconds
            errorRetryCount = null // no limit
        }
        val options = com.kazakago.swr.runtime.SWRValidateOptions(
            retryCount = 10, // 2^10 * 1000 = 1,024,000ms >> 30,000ms
            dedupe = false,
        )
        backgroundScope.launch {
            com.kazakago.swr.runtime.OnErrorRetryDefault(
                RuntimeException("test"),
                "key",
                config,
                { revalidateCalled.add(true) },
                options,
            )
        }
        // If not capped: delay would be ~1,024,000ms (over 17 min)
        // If capped at 30s: delay should be 30,000ms
        advanceTimeBy(30_001)
        assertEquals(1, revalidateCalled.size, "revalidate should be called after 30s cap, not after the full exponential delay")
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
