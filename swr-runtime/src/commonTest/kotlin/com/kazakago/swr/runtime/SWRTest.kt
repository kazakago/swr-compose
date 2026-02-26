package com.kazakago.swr.runtime

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SWRTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validate() = runTest {
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
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(100)
            expectNoEvents()
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun validateFailed() = runTest {
        val error = IllegalStateException()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw error
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(100)
            expectNoEvents()
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
        }
    }

    @Test
    fun nullKeyDoesNotFetch() = runTest {
        var fetchCount = 0
        val swr = SWR<String, String>(
            key = null,
            fetcher = {
                fetchCount++
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(1000)
            expectNoEvents()
            assertEquals(0, fetchCount)
        }
    }

    @Test
    fun sharedCacheBetweenInstances() = runTest {
        val cacheOwner = SWRCacheOwner()
        var fetchCount = 0
        val swr1 = SWR(
            key = "shared-key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateIfStale = false
        }
        swr1.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data-1"), expectMostRecentItem())
        }

        // Second SWR instance with the same key should see the cached data
        val swr2 = SWR(
            key = "shared-key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateIfStale = false
        }
        swr2.stateFlow.test {
            advanceTimeBy(1)
            // Should see cached data without fetching again
            assertEquals(SWRStoreState.Completed("data-1"), expectMostRecentItem())
        }
    }

    @Test
    fun dataAndErrorCoexist() = runTest {
        var callCount = 0
        val error = IllegalStateException()
        val swr = SWR(
            key = "key",
            fetcher = {
                callCount++
                delay(100)
                if (callCount == 1) "data" else throw error
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            shouldRetryOnError = false
            dedupingInterval = kotlin.time.Duration.ZERO
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            // Trigger revalidation that will fail
            swr.mutate()
            advanceTimeBy(201)
            // Data should still be available alongside the error
            val state = expectMostRecentItem()
            assertIs<SWRStoreState.Error<String>>(state)
            assertEquals("data", state.data)
            assertEquals(error, state.error)
        }
    }

    @Test
    fun lifecycleBackgroundToForeground() = runTest {
        var fetchCount = 0
        val lifecycleOwner = TestLifecycleOwner()
        val swr = SWR(
            key = "key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnFocus = true
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(2500)
            assertEquals(SWRStoreState.Completed("data-1"), expectMostRecentItem())

            // Move to background (STARTED) and back to foreground (RESUMED)
            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            advanceTimeBy(100)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading("data-1"), expectMostRecentItem())
            advanceTimeBy(2500)
            assertEquals(SWRStoreState.Completed("data-2"), expectMostRecentItem())
        }
    }

    @Test
    fun defaultConfigPropagation() = runTest {
        val defaultConfig = SWRConfig<Any, Any>().apply {
            dedupingInterval = kotlin.time.Duration.ZERO
        }
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
            defaultConfig = defaultConfig,
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            // With dedupingInterval=0, every mutate should trigger revalidation
            swr.mutate()
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            swr.mutate()
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun mutateUpdatesState() = runTest {
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "original"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("original"), expectMostRecentItem())

            // Mutate with new data and no revalidation
            swr.mutate(data = { "updated" }) {
                revalidate = false
                populateCache = true
            }
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed("updated"), expectMostRecentItem())
        }
    }
}
