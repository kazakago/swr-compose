package com.kazakago.swr.runtime.immutable

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWRImmutable
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
class SWRImmutableTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun immutable() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val swr = SWRImmutable(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        )
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(2500)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            advanceTimeBy(100)
            expectNoEvents()
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)
            advanceTimeBy(1)
            expectNoEvents()
            advanceTimeBy(2500)
            expectNoEvents()
        }
    }
}
