package com.kazakago.swr.runtime.config

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
class DedupingIntervalOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dedupingInterval2Seconds() = runTest {
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
            dedupingInterval = 2.seconds
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            swr.mutate()

            advanceTimeBy(1100)
            expectNoEvents()

            swr.mutate()

            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun dedupingInterval5Seconds() = runTest {
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
            dedupingInterval = 5.seconds
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            swr.mutate()

            advanceTimeBy(1100)
            expectNoEvents()

            swr.mutate()

            advanceTimeBy(1100)
            expectNoEvents()
        }
    }

    @Test
    fun dedupingInterval0Seconds() = runTest {
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
            dedupingInterval = 0.seconds
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            swr.mutate()

            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            swr.mutate()

            advanceTimeBy(1100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }
}
