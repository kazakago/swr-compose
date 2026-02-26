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

@OptIn(ExperimentalCoroutinesApi::class)
class SWRMutateTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validate_mutation() = runTest {
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
                swr.mutate()
            }
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("fetched_2"), expectMostRecentItem())
        }
    }

    @Test
    fun validate_mutationFailed() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        var result: () -> String = { "fetched" }
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
            assertEquals(SWRStoreState.Completed("fetched"), expectMostRecentItem())

            advanceTimeBy(2500)
            val error = IllegalStateException()
            result = { throw error }
            launch {
                swr.mutate()
            }
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Error(data = "fetched", error), expectMostRecentItem())
        }
    }

    @Test
    fun validate_mutationWithData() = runTest {
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
                })
            }
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Loading("mutated"), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("fetched_2"), expectMostRecentItem())
        }
    }

    @Test
    fun validate_mutationWithDataFailed() = runTest {
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
            val error = IllegalStateException()
            result = { "fetched_2" }
            launch {
                val result = swr.mutate(data = {
                    delay(100)
                    throw error
                })
                assertEquals(error, result.exceptionOrNull())
            }
            advanceTimeBy(101)
            expectNoEvents()
        }
    }

    @Test
    fun validateFailed_mutation() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val error = IllegalStateException()
        var result: () -> String = { throw error }
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
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())

            advanceTimeBy(2500)
            result = { "fetched" }
            launch {
                swr.mutate()
            }
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("fetched"), expectMostRecentItem())
            advanceTimeBy(101)
            expectNoEvents()
        }
    }

    @Test
    fun validateFailed_mutationFailed() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val error = IllegalStateException()
        var result: () -> String = { throw error }
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
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())

            advanceTimeBy(2500)
            val error2 = IllegalStateException()
            result = { throw error2 }
            launch {
                swr.mutate()
            }
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Error(null, error2), expectMostRecentItem())
            advanceTimeBy(101)
            expectNoEvents()
        }
    }

    @Test
    fun validateFailed_mutationWithData() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val error = IllegalStateException()
        var result: () -> String = { throw error }
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
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())

            advanceTimeBy(2500)
            result = { "fetched" }
            launch {
                swr.mutate(data = {
                    delay(100)
                    "mutated"
                })
            }
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Loading("mutated"), expectMostRecentItem())
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("fetched"), expectMostRecentItem())
        }
    }

    @Test
    fun validate_mutationWithOptimisticData() = runTest {
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
                    optimisticData = "optimistic"
                    revalidate = false
                }
            }
            advanceTimeBy(1)
            // Optimistic data should appear immediately
            assertEquals(SWRStoreState.Completed("optimistic"), expectMostRecentItem())
            advanceTimeBy(100)
            // After mutation completes, mutated data should appear via populateCache (default true)
            assertEquals(SWRStoreState.Completed("mutated"), expectMostRecentItem())
        }
    }

    @Test
    fun validate_mutationWithOptimisticDataRollback() = runTest {
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
    fun validateFailed_mutationWithDataFailed() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val error = IllegalStateException()
        var result: () -> String = { throw error }
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
            assertEquals(SWRStoreState.Error(null, error), expectMostRecentItem())

            advanceTimeBy(2500)
            val error2 = IllegalStateException()
            val error3 = IllegalStateException()
            result = { throw error2 }
            launch {
                val result = swr.mutate(data = {
                    delay(100)
                    throw error3
                })
                assertEquals(error3, result.exceptionOrNull())
            }
            advanceTimeBy(1000)
            expectNoEvents()
        }
    }
}
