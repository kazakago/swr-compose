package com.kazakago.swr.runtime.config

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
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
class OnLoadingSlowOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withOnLoadingSlow() = runTest {
        val onLoadingSlowList = mutableListOf<String>()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(5.seconds)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            onLoadingSlow = { key, _ ->
                onLoadingSlowList += key
            }
        }
        swr.stateFlow.test {
            skipItems(1)
            advanceTimeBy(3000)
            assertEquals(listOf(), onLoadingSlowList)
            advanceTimeBy(1)
            assertEquals(listOf("key"), onLoadingSlowList)
            skipItems(1)
        }
    }

    @Test
    fun noOnLoadingSlow() = runTest {
        val onLoadingSlowList = mutableListOf<String>()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(2.seconds)
                "data"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            onLoadingSlow = { key, _ ->
                onLoadingSlowList += key
            }
        }
        swr.stateFlow.test {
            skipItems(1)
            advanceTimeBy(3000)
            assertEquals(listOf(), onLoadingSlowList)
            advanceTimeBy(1)
            assertEquals(listOf(), onLoadingSlowList)
            skipItems(1)
        }
    }
}
