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
class SWRInfiniteMutateTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun mutateRevalidation() = runTest {
        var fetchCount = 0
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "key_$pageIndex" },
            fetcher = { key ->
                fetchCount++
                delay(100)
                "fetched_${key}_$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            dedupingInterval = kotlin.time.Duration.ZERO
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0_1")), expectMostRecentItem())

            // Mutate without data triggers revalidation
            launch {
                swr.mutate()
            }
            advanceTimeBy(201)
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0_2")), expectMostRecentItem())
        }
    }

    @Test
    fun mutateWithData() = runTest {
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "key_$pageIndex" },
            fetcher = { key ->
                delay(100)
                "fetched_$key"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            dedupingInterval = kotlin.time.Duration.ZERO
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0")), expectMostRecentItem())

            // Mutate with explicit data and no revalidation
            launch {
                swr.mutate(data = { listOf("manual_data") }) {
                    revalidate = false
                    populateCache = true
                }
            }
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed(listOf("manual_data")), expectMostRecentItem())
        }
    }

    @Test
    fun mutateOptimistic() = runTest {
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "key_$pageIndex" },
            fetcher = { key ->
                delay(100)
                "fetched_$key"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            dedupingInterval = kotlin.time.Duration.ZERO
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0")), expectMostRecentItem())

            // Optimistic update
            launch {
                swr.mutate(data = {
                    delay(100)
                    listOf("final_data")
                }) {
                    optimisticData = listOf("optimistic_data")
                    revalidate = false
                    populateCache = true
                }
            }
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed(listOf("optimistic_data")), expectMostRecentItem())
            advanceTimeBy(100)
            assertEquals(SWRStoreState.Completed(listOf("final_data")), expectMostRecentItem())
        }
    }

    @Test
    fun mutateRollbackOnError() = runTest {
        val error = RuntimeException("mutation failed")
        val swr = SWRInfinite<String, String>(
            getKey = { pageIndex, _ -> "key_$pageIndex" },
            fetcher = { key ->
                delay(100)
                "fetched_$key"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            dedupingInterval = kotlin.time.Duration.ZERO
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0")), expectMostRecentItem())

            // Optimistic update then error with rollback
            launch {
                swr.mutate(data = {
                    delay(100)
                    throw error
                }) {
                    optimisticData = listOf("optimistic")
                    rollbackOnError = true
                    revalidate = false
                }
            }
            advanceTimeBy(1)
            assertEquals(SWRStoreState.Completed(listOf("optimistic")), expectMostRecentItem())
            advanceTimeBy(100)
            // Should rollback to original data
            assertEquals(SWRStoreState.Completed(listOf("fetched_key_0")), expectMostRecentItem())
        }
    }
}
