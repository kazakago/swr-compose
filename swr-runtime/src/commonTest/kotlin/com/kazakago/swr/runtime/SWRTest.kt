package com.kazakago.swr.runtime

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
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
class SWRTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var lifecycleOwner: TestLifecycleOwner

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        lifecycleOwner = TestLifecycleOwner()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validate() = runTest {
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
        )
        swr.stateFlow.test {
            expectMostRecentItem().apply {
                assertEquals(SWRStoreState.Loading(null), this)
            }
            advanceTimeBy(100)
            expectNoEvents()
            advanceTimeBy(1)
            expectMostRecentItem().apply {
                assertEquals(SWRStoreState.Completed("data"), this)
            }
        }
    }

    @Test
    fun validateFailed() = runTest {
        val error = IllegalStateException()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw error
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
        )
        swr.stateFlow.test {
            expectMostRecentItem().apply {
                assertEquals(SWRStoreState.Loading(null), this)
            }
            advanceTimeBy(100)
            expectNoEvents()
            advanceTimeBy(1)
            expectMostRecentItem().apply {
                assertEquals(SWRStoreState.Error(null, error), this)
            }
        }
    }
}
