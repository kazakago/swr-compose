package com.kazakago.swr.runtime.mutation

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class OptimisticOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withOptimisticData() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        var result: () -> String = { "fetched_1" }
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                result()
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("fetched_1"), expectMostRecentItem())

            advanceTimeBy(2500)
            result = { "fetched_2" }
            launch {
                swr.mutate(data = {
                    delay(100)
                    "mutated"
                }) {
                    optimisticData = "optimisticData"
                }
            }
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed("optimisticData"), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Loading("mutated"), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Completed("fetched_2"), expectMostRecentItem())
        }
    }

    @Test
    fun withOptimisticDataRollbackOnError() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val error = IllegalStateException()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "fetched"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("fetched"), expectMostRecentItem())

            advanceTimeBy(2500)
            launch {
                swr.mutate(data = {
                    delay(100)
                    throw error
                }) {
                    optimisticData = "optimistic"
                    rollbackOnError = true
                }
            }
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed("optimistic"), expectMostRecentItem())
            advanceTimeBy(100)
            // Rollback restores previous data with keepState=true (Fixed state preserved)
            assertEquals(SWRStoreState.Completed("fetched"), expectMostRecentItem())
        }
    }

    @Test
    fun noOptimisticData() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "fetched"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("fetched"), expectMostRecentItem())

            advanceTimeBy(2500)
            launch {
                swr.mutate(data = {
                    delay(100)
                    "mutated"
                }) {
                    // No optimisticData set — state should not change immediately
                }
            }
            advanceTimeBy(1)
            expectNoEvents() // No immediate state change without optimistic data
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Loading("mutated"), expectMostRecentItem())
        }
    }
}
