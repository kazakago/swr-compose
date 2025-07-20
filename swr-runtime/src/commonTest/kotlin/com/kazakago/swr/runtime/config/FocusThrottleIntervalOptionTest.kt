package com.kazakago.swr.runtime.config

import androidx.lifecycle.Lifecycle
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class FocusThrottleIntervalOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun focusThrottleInterval5Seconds() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnFocus = true
            focusThrottleInterval = 5.seconds
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            expectNoEvents()

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            expectNoEvents()

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun focusThrottleInterval10Seconds() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnFocus = true
            focusThrottleInterval = 10.seconds
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            expectNoEvents()

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            expectNoEvents()

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            expectNoEvents()

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            expectNoEvents()

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }

    @Test
    fun focusThrottleInterval0Seconds() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnFocus = true
            focusThrottleInterval = 0.seconds
        }
        swr.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())

            lifecycleOwner.setCurrentState(Lifecycle.State.STARTED)
            lifecycleOwner.setCurrentState(Lifecycle.State.RESUMED)

            advanceTimeBy(2100)
            assertEquals(SWRStoreState.Completed("data"), expectMostRecentItem())
        }
    }
}
