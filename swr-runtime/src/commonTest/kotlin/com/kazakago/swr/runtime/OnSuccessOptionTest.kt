package com.kazakago.swr.runtime

import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
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
class OnSuccessOptionTest {

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
    fun onSuccess() = runTest {
        val onSuccessList = mutableListOf<Pair<String, String>>()
        val swr = SWR(
            key = "key",
            fetcher = {
                delay(100)
                "data"
            },
            lifecycleOwner = lifecycleOwner,
            scope = backgroundScope,
            cacheOwner = SWRCacheOwner(),
        ) {
            onSuccess = { data, key, _ ->
                onSuccessList += data to key
            }
            onError = { _, _, _ ->
                fail("Must not reach here")
            }
        }
        swr.stateFlow.test {
            skipItems(2)
            assertEquals(listOf("data" to "key"), onSuccessList)
        }
    }
}
