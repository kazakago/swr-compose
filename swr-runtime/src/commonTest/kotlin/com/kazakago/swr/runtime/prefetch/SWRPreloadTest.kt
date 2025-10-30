package com.kazakago.swr.runtime.prefetch

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.SWRPreload
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

@OptIn(ExperimentalCoroutinesApi::class)
class SWRPreloadTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun prefetch() = runTest {
        val fetcher: suspend (String) -> String = {
            delay(100)
            "data"
        }
        val cacheOwner = SWRCacheOwner()
        val swr = SWR(
            key = "key",
            fetcher = fetcher,
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnMount = false
        }
        val preload = SWRPreload(
            key = "key",
            fetcher = fetcher,
            scope = backgroundScope,
            cacheOwner = cacheOwner,
        )
        launch {
            preload()
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
            advanceTimeBy(1)
            expectNoEvents()
        }
    }

    @Test
    fun prefetchFailed() = runTest {
        val error = IllegalStateException()
        val fetcher: suspend (String) -> String = {
            delay(100)
            throw error
        }
        val cacheOwner = SWRCacheOwner()
        val swr = SWR(
            key = "key",
            fetcher = fetcher,
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnMount = false
        }
        val preload = SWRPreload(
            key = "key",
            fetcher = fetcher,
            scope = backgroundScope,
            cacheOwner = cacheOwner,
        )
        launch {
            preload()
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
            advanceTimeBy(1)
            expectNoEvents()
        }
    }
}
