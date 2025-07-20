package com.kazakago.swr.runtime.config

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
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
class ErrorRetryCountOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun errorRetryCountNull() = runTest {
        val errorRetryCountList = mutableListOf<Int?>()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw IllegalStateException()
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            errorRetryCount = null
            onErrorRetry = { _, _, config, _, _ ->
                errorRetryCountList += config.errorRetryCount
            }
        }
        swr.stateFlow.test {
            advanceTimeBy(1000)
            skipItems(2)
            assertEquals(listOf<Int?>(null), errorRetryCountList)
        }
    }

    @Test
    fun errorRetryCount3() = runTest {
        val errorRetryCountList = mutableListOf<Int?>()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                throw IllegalStateException()
            },
            lifecycleOwner = TestLifecycleOwner(),
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
            networkMonitor = TestNetworkMonitor(),
        ) {
            errorRetryCount = 3
            onErrorRetry = { _, _, config, _, _ ->
                errorRetryCountList += config.errorRetryCount
            }
        }
        swr.stateFlow.test {
            advanceTimeBy(1000)
            skipItems(2)
            assertEquals(listOf<Int?>(3), errorRetryCountList)
        }
    }
}
