package com.kazakago.swr.runtime

import app.cash.turbine.test
import com.kazakago.swr.store.SWRStore
import com.kazakago.swr.store.SWRStoreState
import com.kazakago.swr.store.cache.SWRCacheOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SWRSubscriptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun data_received_updates_cache() = runTest {
        val channel = Channel<String>()
        val subscription = SWRSubscription<String, String>(
            key = "key",
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            subscribe = { _ -> channel.receiveAsFlow() },
        )
        subscription.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            channel.send("hello")
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("hello"), expectMostRecentItem())
        }
        subscription.cancel()
    }

    @Test
    fun error_in_flow_updates_error_state() = runTest {
        val error = RuntimeException("boom")
        val subscription = SWRSubscription<String, String>(
            key = "key",
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            subscribe = { _ -> flow { throw error } },
        )
        advanceTimeBy(101)
        assertEquals(error, subscription.error.value)
    }

    @Test
    fun data_received_clears_error() = runTest {
        val channel = Channel<String>()
        val subscription = SWRSubscription<String, String>(
            key = "key",
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            subscribe = { _ -> channel.receiveAsFlow() },
        )
        assertNull(subscription.error.value)
        channel.send("data")
        advanceTimeBy(101)
        assertNull(subscription.error.value)
        subscription.cancel()
    }

    @Test
    fun null_key_does_not_subscribe() = runTest {
        var subscribed = false
        val subscription = SWRSubscription<String, String>(
            key = null,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            subscribe = { _ ->
                subscribed = true
                flow {}
            },
        )
        advanceTimeBy(101)
        assertFalse(subscribed)
    }

    @Test
    fun cancel_stops_collection() = runTest {
        val cancelled = CompletableDeferred<Boolean>()
        val subscription = SWRSubscription<String, String>(
            key = "key",
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            subscribe = { _ ->
                flow {
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled.complete(true)
                    }
                }
            },
        )
        advanceTimeBy(101)
        assertFalse(cancelled.isCompleted)
        subscription.cancel()
        advanceTimeBy(101)
        assertTrue(cancelled.isCompleted)
    }

    @Test
    fun cache_integration_shared_store_receives_subscription_data() = runTest {
        val cacheOwner = SWRCacheOwner()
        val channel = Channel<String>()
        val subscription = SWRSubscription<String, String>(
            key = "shared_key",
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            subscribe = { _ -> channel.receiveAsFlow() },
        )
        val store = SWRStore<String, String>(
            key = "shared_key",
            fetcher = { error("should not be called") },
            cacheOwner = cacheOwner,
        )
        channel.send("shared_data")
        advanceTimeBy(101)
        store.flow.test {
            assertEquals(SWRStoreState.Completed("shared_data"), expectMostRecentItem())
        }
        subscription.cancel()
    }

    @Test
    fun periodic_updates() = runTest {
        val subscription = SWRSubscription<String, Int>(
            key = "key",
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            subscribe = { _ ->
                flow {
                    var count = 0
                    while (true) {
                        delay(1000L)
                        count++
                        emit(count)
                    }
                }
            },
        )
        subscription.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            advanceTimeBy(1001)
            assertEquals(SWRStoreState.Completed(1), expectMostRecentItem())
            advanceTimeBy(1000)
            assertEquals(SWRStoreState.Completed(2), expectMostRecentItem())
        }
        subscription.cancel()
    }

    @Test
    fun multiple_emissions_update_cache_sequentially() = runTest {
        val channel = Channel<String>()
        val subscription = SWRSubscription<String, String>(
            key = "key",
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            subscribe = { _ -> channel.receiveAsFlow() },
        )
        subscription.stateFlow.test {
            assertEquals(SWRStoreState.Loading(null), expectMostRecentItem())
            channel.send("first")
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("first"), expectMostRecentItem())
            channel.send("second")
            advanceTimeBy(101)
            assertEquals(SWRStoreState.Completed("second"), expectMostRecentItem())
        }
        subscription.cancel()
    }
}
