package com.kazakago.swr.runtime.options

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.store.SWRStoreState
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class IsPausedOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun isPausedSuppressesMountFetch() = runTest {
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            isPaused = { true }
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            advanceTimeBy(1100)
            // Fetch should be suppressed, state remains Loading
            expectNoEvents()
        }
    }

    @Test
    fun isPausedDynamicToggle() = runTest {
        var paused = false
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            isPaused = { paused }
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            // Not paused initially, so fetch completes
            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            // Now pause and try to mutate
            paused = true
            swr.mutate()

            advanceTimeBy(1100)
            // Should not revalidate while paused
            expectNoEvents()

            // Unpause and mutate again
            paused = false
            swr.mutate()

            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun isPausedSuppressesPolling() = runTest {
        var fetchCount = 0
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                fetchCount++
                "data$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            isPaused = { true }
            refreshInterval = 1.seconds
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            // Wait for multiple polling intervals
            advanceTimeBy(5100)
            // All fetches should be suppressed
            expectNoEvents()
            assertEquals(0, fetchCount)
        }
    }
}
