package com.kazakago.swr.runtime.options

import androidx.lifecycle.Lifecycle
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
class RefreshWhenHiddenOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun noRefreshWhenHidden() = runTest {
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.CREATED)
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            refreshInterval = 10.seconds
            refreshWhenHidden = false
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
            advanceTimeBy(9900)
            expectNoEvents()
            advanceTimeBy(101)
            expectNoEvents()
        }
    }

    @Test
    fun withRefreshWhenHidden() = runTest {
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.CREATED)
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            refreshInterval = 10.seconds
            refreshWhenHidden = true
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
            advanceTimeBy(9900)
            assertEquals(SWRStoreState.Loading("data"), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }
}
