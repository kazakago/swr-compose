package com.kazakago.swr.runtime

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.store.GettingFrom
import com.kazakago.swr.store.SWRStore
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SWRMutationTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun trigger_success() = runTest {
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg -> "result:$arg" },
            cacheOwner = SWRCacheOwner(),
        )
        val result = mutation.trigger("hello")
        assertTrue(result.isSuccess)
        assertEquals("result:hello", result.getOrNull())
        assertEquals("result:hello", mutation.data.value)
        assertNull(mutation.error.value)
        assertFalse(mutation.isMutating.value)
    }

    @Test
    fun trigger_failure() = runTest {
        val error = RuntimeException("boom")
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, _ -> throw error },
            cacheOwner = SWRCacheOwner(),
        )
        val result = mutation.trigger("hello")
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        assertNull(mutation.data.value)
        assertEquals(error, mutation.error.value)
        assertFalse(mutation.isMutating.value)
    }

    @Test
    fun trigger_isMutating() = runTest {
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg ->
                delay(100)
                "result:$arg"
            },
            cacheOwner = SWRCacheOwner(),
        )
        assertFalse(mutation.isMutating.value)
        backgroundScope.launch { mutation.trigger("hello") }
        advanceTimeBy(50)
        assertTrue(mutation.isMutating.value)
        advanceTimeBy(51)
        assertFalse(mutation.isMutating.value)
    }

    @Test
    fun trigger_populateCache() = runTest {
        val cacheOwner = SWRCacheOwner()
        val swrStore = SWRStore(
            key = "key",
            fetcher = { "original" },
            cacheOwner = cacheOwner,
        )
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg -> "mutated:$arg" },
            cacheOwner = cacheOwner,
        )
        mutation.trigger("hello") {
            populateCache = true
        }
        val cached = swrStore.get(GettingFrom.LocalOnly).getOrNull()
        assertEquals("mutated:hello", cached)
    }

    @Test
    fun trigger_populateCache_false() = runTest {
        val cacheOwner = SWRCacheOwner()
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg -> "mutated:$arg" },
            cacheOwner = cacheOwner,
        )
        mutation.trigger("hello") {
            populateCache = false
        }
        val swrStore = SWRStore(
            key = "key",
            fetcher = { "original" },
            cacheOwner = cacheOwner,
        )
        val cached = swrStore.get(GettingFrom.LocalOnly).getOrNull()
        assertNull(cached)
    }

    @Test
    fun trigger_optimisticData() = runTest {
        val cacheOwner = SWRCacheOwner()
        val swrStore = SWRStore(
            key = "key",
            fetcher = { "original" },
            cacheOwner = cacheOwner,
        )
        swrStore.update("initial", false)

        val seenValues = mutableListOf<String?>()
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, _ ->
                val v = swrStore.get(GettingFrom.LocalOnly).getOrNull()
                seenValues.add(v)
                "final"
            },
            cacheOwner = cacheOwner,
        )
        mutation.trigger("hello") {
            optimisticData = "optimistic"
            populateCache = true
        }
        assertEquals("optimistic", seenValues.firstOrNull())
        val cached = swrStore.get(GettingFrom.LocalOnly).getOrNull()
        assertEquals("final", cached)
    }

    @Test
    fun trigger_rollbackOnError() = runTest {
        val cacheOwner = SWRCacheOwner()
        val swrStore = SWRStore(
            key = "key",
            fetcher = { "original" },
            cacheOwner = cacheOwner,
        )
        swrStore.update("initial", false)

        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, _ -> throw RuntimeException("error") },
            cacheOwner = cacheOwner,
        )
        mutation.trigger("hello") {
            optimisticData = "optimistic"
            rollbackOnError = true
        }
        val cached = swrStore.get(GettingFrom.LocalOnly).getOrNull()
        assertEquals("initial", cached)
    }

    @Test
    fun trigger_reset() = runTest {
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg -> "result:$arg" },
            cacheOwner = SWRCacheOwner(),
        )
        mutation.trigger("hello")
        assertEquals("result:hello", mutation.data.value)
        mutation.reset()
        assertNull(mutation.data.value)
        assertNull(mutation.error.value)
        assertFalse(mutation.isMutating.value)
    }

    @Test
    fun trigger_withNullKey() = runTest {
        val mutation = SWRMutation<String, String, String>(
            key = null,
            fetcher = { _, arg -> "result:$arg" },
            cacheOwner = SWRCacheOwner(),
        )
        val result = mutation.trigger("hello")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun trigger_onSuccess_callback() = runTest {
        val received = mutableListOf<String>()
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg -> "result:$arg" },
            cacheOwner = SWRCacheOwner(),
        )
        mutation.trigger("hello") {
            onSuccess = { data, _, _ -> received.add(data) }
        }
        assertEquals(listOf("result:hello"), received)
    }

    @Test
    fun trigger_onError_callback() = runTest {
        val error = RuntimeException("boom")
        val received = mutableListOf<Throwable>()
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, _ -> throw error },
            cacheOwner = SWRCacheOwner(),
        )
        mutation.trigger("hello") {
            onError = { err, _, _ -> received.add(err) }
        }
        assertEquals(listOf<Throwable>(error), received)
    }

    @Test
    fun trigger_overrideConfig() = runTest {
        val cacheOwner = SWRCacheOwner()
        val swrStore = SWRStore(
            key = "key",
            fetcher = { "original" },
            cacheOwner = cacheOwner,
        )
        swrStore.update("initial", false)

        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg -> "mutated:$arg" },
            cacheOwner = cacheOwner,
        ) {
            // Default config: populateCache = false
            populateCache = false
        }
        // Override populateCache to true in trigger
        mutation.trigger("hello") {
            populateCache = true
            revalidate = false
        }
        val cached = swrStore.get(GettingFrom.LocalOnly).getOrNull()
        assertEquals("mutated:hello", cached, "trigger lambda should override default config")
    }

    @Test
    fun trigger_multiple_sequential() = runTest {
        val results = mutableListOf<String>()
        val mutation = SWRMutation<String, String, String>(
            key = "key",
            fetcher = { _, arg ->
                delay(50)
                "result:$arg"
            },
            cacheOwner = SWRCacheOwner(),
        )
        val r1 = mutation.trigger("first") { revalidate = false }
        results.add(r1.getOrNull()!!)
        val r2 = mutation.trigger("second") { revalidate = false }
        results.add(r2.getOrNull()!!)
        val r3 = mutation.trigger("third") { revalidate = false }
        results.add(r3.getOrNull()!!)

        assertEquals(listOf("result:first", "result:second", "result:third"), results)
        assertEquals("result:third", mutation.data.value)
    }

    @Test
    fun trigger_revalidate_default() = runTest {
        var fetchCount = 0
        val cacheOwner = SWRCacheOwner()
        val swr = SWR(
            key = "key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnFocus = false
            revalidateOnReconnect = false
            dedupingInterval = kotlin.time.Duration.ZERO
        }
        swr.stateFlow.test {
            // Initial fetch
            advanceTimeBy(201)
            assertEquals(1, fetchCount)
            val initialState = expectMostRecentItem()
            assertEquals(SWRStoreState.Completed("data-1"), initialState)

            // Mutation with revalidate=true (default)
            val mutation = SWRMutation<String, String, String>(
                key = "key",
                fetcher = { _, arg -> "mutated:$arg" },
                cacheOwner = cacheOwner,
            )
            mutation.trigger("hello")
            advanceTimeBy(201)
            // SWR should have revalidated
            assertTrue(fetchCount >= 2, "Expected fetchCount >= 2, but was $fetchCount")
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun trigger_revalidate_false() = runTest {
        var fetchCount = 0
        val cacheOwner = SWRCacheOwner()
        val swr = SWR(
            key = "key",
            fetcher = {
                fetchCount++
                delay(100)
                "data-$fetchCount"
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = cacheOwner,
            networkMonitor = TestNetworkMonitor(),
        ) {
            revalidateOnFocus = false
            revalidateOnReconnect = false
        }
        swr.stateFlow.test {
            // Initial fetch
            advanceTimeBy(201)
            assertEquals(1, fetchCount)
            expectMostRecentItem()

            // Mutation with revalidate=false
            val mutation = SWRMutation<String, String, String>(
                key = "key",
                fetcher = { _, arg -> "mutated:$arg" },
                cacheOwner = cacheOwner,
            )
            mutation.trigger("hello") {
                revalidate = false
            }
            advanceTimeBy(201)
            // SWR should NOT have revalidated
            assertEquals(1, fetchCount, "Expected fetchCount to still be 1 when revalidate=false")
            cancelAndConsumeRemainingEvents()
        }
    }
}
