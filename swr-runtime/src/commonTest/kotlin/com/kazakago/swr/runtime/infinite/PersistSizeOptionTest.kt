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
class PersistSizeOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withPersistSize() = runTest {
        var version = "v1"
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "page_${pageIndex}_${version}" },
            fetcher = {
                delay(100)
                it
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            persistSize = true
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            assertEquals(1, swr.getSize())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("page_0_v1")), expectMostRecentItem())
            assertEquals(1, swr.getSize())

            swr.setSize(swr.getSize() + 1)
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading(listOf("page_0_v1", null)), expectMostRecentItem())
            assertEquals(2, swr.getSize())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Completed(listOf("page_0_v1", "page_1_v1")), expectMostRecentItem())
            assertEquals(2, swr.getSize())

            version = "v2"
            swr.setSize(swr.getSize() + 2)
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            assertEquals(4, swr.getSize())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Completed(listOf("page_0_v2", "page_1_v2", "page_2_v2", "page_3_v2")), expectMostRecentItem())
            assertEquals(4, swr.getSize())
        }
    }

    @Test
    fun noPersistSize() = runTest {
        var version = "v1"
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "page_${pageIndex}_${version}" },
            fetcher = {
                delay(100)
                it
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            persistSize = false
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            assertEquals(1, swr.getSize())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("page_0_v1")), expectMostRecentItem())
            assertEquals(1, swr.getSize())

            swr.setSize(swr.getSize() + 1)
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading(listOf("page_0_v1", null)), expectMostRecentItem())
            assertEquals(2, swr.getSize())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Completed(listOf("page_0_v1", "page_1_v1")), expectMostRecentItem())
            assertEquals(2, swr.getSize())

            version = "v2"
            swr.setSize(swr.getSize() + 2)
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            assertEquals(1, swr.getSize())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Completed(listOf("page_0_v2")), expectMostRecentItem())
            assertEquals(1, swr.getSize())
        }
    }
}
