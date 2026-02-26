package com.kazakago.swr.runtime.immutable

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWRImmutable
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.store.SWRStoreState
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SWRImmutableTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun immutable() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val swr = SWRImmutable(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(2500)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            advanceTimeBy(100)
            expectNoEvents()
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)
            advanceTimeBy(1)
            expectNoEvents()
            advanceTimeBy(2500)
            expectNoEvents()
        }
    }

    @Test
    fun immutableError() = runTest {
        val error = RuntimeException("fetch failed")
        val swr = SWRImmutable(
            key = "key",
            fetcher = { _: String ->
                delay(100)
                throw error
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            shouldRetryOnError = false
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            val state = expectMostRecentItem()
            assertIs<SWRStoreState.Error<String>>(state)
            assertEquals(error, state.error)
        }
    }

    @Test
    fun immutableManualMutate() = runTest {
        var fetchCount = 0
        val swr = SWRImmutable(
            key = "key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            dedupingInterval = kotlin.time.Duration.ZERO
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data-1"), expectMostRecentItem())

            // Manual mutate should still work even in immutable mode
            launch {
                swr.mutate()
            }
            advanceTimeBy(201)
            assertEquals(SWRStoreState.Completed("data-2"), expectMostRecentItem())
        }
    }

    @Test
    fun immutableWithCachedData() = runTest {
        val cacheOwner = SWRCacheOwner()
        var fetchCount = 0

        // First SWRImmutable populates cache
        val swr1 = SWRImmutable(
            key = "key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        )
        swr1.stateFlow.test {
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data-1"), expectMostRecentItem())
        }

        // Second SWRImmutable should use cached data without fetching
        val swr2 = SWRImmutable(
            key = "key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        )
        swr2.stateFlow.test {
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed("data-1"), expectMostRecentItem())
            // No additional fetch should have been made
            assertEquals(1, fetchCount)
        }
    }
}
