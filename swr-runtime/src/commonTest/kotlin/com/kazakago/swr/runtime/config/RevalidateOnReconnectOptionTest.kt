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

@OptIn(ExperimentalCoroutinesApi::class)
class RevalidateOnReconnectOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withRevalidateOnReconnect() = runTest {
        val networkMonitor = TestNetworkMonitor()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = networkMonitor,
        ) {
            revalidateOnReconnect = true
        }
        swr.stateFlow.test {
            advanceTimeBy(2500)
            skipItems(2)

            networkMonitor.onlineStatus.value = false
            advanceTimeBy(1)
            networkMonitor.onlineStatus.value = true
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Loading("data"), expectMostRecentItem())
            advanceTimeBy(2500)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun noRevalidateOnReconnect() = runTest {
        val networkMonitor = TestNetworkMonitor()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = networkMonitor,
        ) {
            revalidateOnReconnect = false
        }
        swr.stateFlow.test {
            advanceTimeBy(2500)
            skipItems(2)

            networkMonitor.onlineStatus.value = false
            advanceTimeBy(1)
            networkMonitor.onlineStatus.value = true
            advanceTimeBy(1)
            expectNoEvents()
            advanceTimeBy(2500)
            expectNoEvents()
        }
    }
}
