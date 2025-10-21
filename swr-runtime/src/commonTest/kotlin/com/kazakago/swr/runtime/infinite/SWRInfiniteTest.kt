package com.kazakago.swr.runtime.infinite

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWRInfinite
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

@OptIn(ExperimentalCoroutinesApi::class)
class SWRInfiniteTest {

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
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "key_${pageIndex}" },
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
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("data")), expectMostRecentItem())
        }
    }

    @Test
    fun incrementSetSize() = runTest {
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "key_${pageIndex}" },
            fetcher = {
                delay(100)
                "fetched_$it"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            assertEquals(1, swr.getSize())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0")), expectMostRecentItem())
            assertEquals(1, swr.getSize())

            swr.setSize(swr.getSize() + 1)
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading(listOf("fetched_key_0", null)), expectMostRecentItem())
            assertEquals(2, swr.getSize())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0", "fetched_key_1")), expectMostRecentItem())
            assertEquals(2, swr.getSize())
        }
    }
}
