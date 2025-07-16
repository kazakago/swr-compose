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
import kotlin.math.exp
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OnErrorRetryOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultOnErrorRetry() = runTest {
        val error = IllegalStateException()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw error
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(100)
            expectNoEvents()
            advanceTimeBy(15000)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
        }
    }

    @Test
    fun customOnErrorRetry() = runTest {
        val error = IllegalStateException()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw error
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            onErrorRetry = { _, _, _, revalidate, options ->
                delay(100)
                revalidate(options)
            }
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
        }
    }
}
