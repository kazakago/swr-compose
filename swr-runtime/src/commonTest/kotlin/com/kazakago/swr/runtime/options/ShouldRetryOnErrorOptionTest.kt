package com.kazakago.swr.runtime.options

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
class ShouldRetryOnErrorOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withShouldRetryOnError() = runTest {
        val error = IllegalStateException()
        val swr = SWR<String, String>(
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
            shouldRetryOnError = true
            onErrorRetry = { _, _, _, revalidate, options ->
                delay(5000)
                revalidate(options)
            }
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())

            advanceTimeBy(5000)
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())
        }
    }

    @Test
    fun noShouldRetryOnError() = runTest {
        val error = IllegalStateException()
        val swr = SWR<String, String>(
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
            shouldRetryOnError = false
            onErrorRetry = { _, _, _, revalidate, options ->
                delay(5000)
                revalidate(options)
            }
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())

            advanceTimeBy(5000)
            expectNoEvents()
            advanceTimeBy(100)
            expectNoEvents()
        }
    }
}
