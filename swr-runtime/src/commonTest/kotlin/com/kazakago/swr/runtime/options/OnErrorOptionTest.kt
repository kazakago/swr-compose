package com.kazakago.swr.runtime.options

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.kazakago.swr.runtime.SWR
import com.kazakago.swr.runtime.internal.TestNetworkMonitor
import com.kazakago.swr.store.cache.SWRCacheOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class OnErrorOptionTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onError() = runTest {
        val error: Throwable = IllegalStateException()
        val onErrorList = mutableListOf<Pair<Throwable, String>>()
        val swr = SWR(
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
            onSuccess = { _, _, _ ->
                fail("Must not reach here")
            }
            onError = { error, key, _ ->
                onErrorList += error to key
            }
        }
        swr.stateFlow.test {
            skipItems(2)
            assertEquals(listOf(error to "key"), onErrorList)
        }
    }
}
