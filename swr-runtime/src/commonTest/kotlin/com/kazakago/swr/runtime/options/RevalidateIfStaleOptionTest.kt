package com.kazakago.swr.runtime.options

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.store.SWRStoreState
import com.kazakago.swr.store.cache.SWRCache
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
class RevalidateIfStaleOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withRevalidateIfStale() = runTest {
        val cacheKey = "key"
        val cacheOwner = SWRCacheOwner().apply {
            cacheMap[cacheKey] = SWRCache().apply {
                data = "cache"
            }
        }
        val swr = SWR(
            key = cacheKey,
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateIfStale = true
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading("cache"), expectMostRecentItem())

            advanceTimeBy(2500)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun noRevalidateIfStale() = runTest {
        val cacheKey = "key"
        val cacheOwner = SWRCacheOwner().apply {
            cacheMap[cacheKey] = SWRCache().apply {
                data = "cache"
            }
        }
        val swr = SWR(
            key = cacheKey,
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateIfStale = false
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed("cache"), expectMostRecentItem())

            advanceTimeBy(2500)
            expectNoEvents()
        }
    }
}
