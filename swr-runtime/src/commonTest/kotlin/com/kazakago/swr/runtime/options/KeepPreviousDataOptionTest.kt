package com.kazakago.swr.runtime.options

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.KeepPreviousDataHolder
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.runtime.withKeepPreviousData
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

@OptIn(ExperimentalCoroutinesApi::class)
class KeepPreviousDataOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withKeepPreviousData() = runTest {
        val holder = KeepPreviousDataHolder<String>()
        val cacheOwner = SWRCacheOwner()

        // 1st SWR with key1
        val swr1 = SWR(
            key = "key1",
            fetcher = {
                delay(100)
                "data1"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = true
        }
        swr1.stateFlow.withKeepPreviousData(holder).test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data1"), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        // 2nd SWR with key2 (simulates key change)
        val swr2 = SWR(
            key = "key2",
            fetcher = {
                delay(100)
                "data2"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = true
        }
        swr2.stateFlow.withKeepPreviousData(holder).test {
            // Previous data preserved during loading
            assertEquals(SWRStoreState.Loading("data1"), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data2"), expectMostRecentItem())
        }
    }

    @Test
    fun noKeepPreviousData() = runTest {
        val cacheOwner = SWRCacheOwner()

        // 1st SWR with key1
        val swr1 = SWR(
            key = "key1",
            fetcher = {
                delay(100)
                "data1"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = false
        }
        swr1.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data1"), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        // 2nd SWR with key2 — without keepPreviousData, data is null
        val swr2 = SWR(
            key = "key2",
            fetcher = {
                delay(100)
                "data2"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = false
        }
        swr2.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data2"), expectMostRecentItem())
        }
    }

    @Test
    fun withKeepPreviousDataRapidKeyChanges() = runTest {
        val holder = KeepPreviousDataHolder<String>()
        val cacheOwner = SWRCacheOwner()

        // Initial SWR completes
        val swr1 = SWR(
            key = "key1",
            fetcher = {
                delay(100)
                "data1"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = true
        }
        swr1.stateFlow.withKeepPreviousData(holder).test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data1"), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Rapid key change: key2 starts loading but we switch to key3 before it completes
        val swr2 = SWR(
            key = "key2",
            fetcher = {
                delay(200)
                "data2"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = true
        }
        swr2.stateFlow.withKeepPreviousData(holder).test {
            // Should show previous data1 during loading
            assertEquals(SWRStoreState.Loading("data1"), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Switch to key3 while key2 is still loading — holder should still have data1
        val swr3 = SWR(
            key = "key3",
            fetcher = {
                delay(100)
                "data3"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = true
        }
        swr3.stateFlow.withKeepPreviousData(holder).test {
            // Still shows data1 since key2 never completed
            assertEquals(SWRStoreState.Loading("data1"), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data3"), expectMostRecentItem())
        }
    }

    @Test
    fun withKeepPreviousDataOnError() = runTest {
        val holder = KeepPreviousDataHolder<String>()
        val cacheOwner = SWRCacheOwner()

        // 1st SWR succeeds
        val swr1 = SWR(
            key = "key1",
            fetcher = {
                delay(100)
                "data1"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = true
        }
        swr1.stateFlow.withKeepPreviousData(holder).test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data1"), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        // 2nd SWR fails — previous data preserved in error state
        val error = RuntimeException("fetch failed")
        val swr2 = SWR(
            key = "key2",
            fetcher = { _: String ->
                delay(100)
                throw error
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            keepPreviousData = true
            shouldRetryOnError = false
        }
        swr2.stateFlow.withKeepPreviousData(holder).test {
            // Previous data preserved during loading
            assertEquals(SWRStoreState.Loading("data1"), expectMostRecentItem())
            advanceTimeBy(101)
            // Previous data preserved in error state
            assertEquals(SWRStoreState.Error("data1", error), expectMostRecentItem())
        }
    }
}
