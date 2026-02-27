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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ParallelOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withParallel() = runTest {
        val receivedPreviousPageData = mutableListOf<String?>()
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, previousPageData ->
                receivedPreviousPageData.add(previousPageData)
                "page_${pageIndex}"
            },
            fetcher = {
                delay(100)
                "${it}_data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            parallel = true
            initialSize = 3
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("page_0_data", "page_1_data", "page_2_data")), expectMostRecentItem())
            // All previousPageData should be null in parallel mode
            assertEquals(3, receivedPreviousPageData.size)
            receivedPreviousPageData.forEach { assertNull(it) }
        }
    }

    @Test
    fun noParallel() = runTest {
        val receivedPreviousPageData = mutableListOf<String?>()
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, previousPageData ->
                receivedPreviousPageData.add(previousPageData)
                "page_${pageIndex}"
            },
            fetcher = {
                delay(100)
                "${it}_data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            parallel = false
            initialSize = 3
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("page_0_data", "page_1_data", "page_2_data")), expectMostRecentItem())
            // First page has null previousPageData, subsequent pages may have null (no cache yet)
            assertEquals(3, receivedPreviousPageData.size)
            assertNull(receivedPreviousPageData[0]) // First page always null
        }
    }
}
