package com.kazakago.swr.store

import app.cash.turbine.test
import com.kazakago.swr.store.cache.defaultSWRCacheOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

class SWRStoreTest {

    @BeforeTest
    fun setUp() {
        defaultSWRCacheOwner.clearAll()
    }

    @Test
    fun validate_Success() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        launch {
            swrStore.validate()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            expectNoEvents()
        }
    }

    @Test
    fun validate_Error() = runTest {
        val swrStore = SWRStore<String, String>(
            key = "KEY",
            fetcher = { throw NoSuchElementException() },
        )
        launch {
            swrStore.validate()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Error<String>>(this)
                assertEquals(null, data)
                assertIs<NoSuchElementException>(error)
            }
            expectNoEvents()
        }
    }

    @Test
    fun validate_Error_Success() = runTest {
        var fetcher: (key: String) -> String = { throw NoSuchElementException() }
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { fetcher(it) },
        )
        launch {
            swrStore.validate()
            fetcher = { "DATA" }
            swrStore.validate()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Error<String>>(this)
                assertEquals(null, data)
                assertIs<NoSuchElementException>(error)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            expectNoEvents()
        }
    }

    @Test
    fun validate_Success_Error() = runTest {
        var fetcher: (key: String) -> String = { "DATA" }
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { fetcher(it) },
        )
        launch {
            swrStore.validate()
            fetcher = { throw NoSuchElementException() }
            swrStore.validate()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals("DATA", data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Error<String>>(this)
                assertEquals("DATA", data)
                assertIs<NoSuchElementException>(error)
            }
            expectNoEvents()
        }
    }

    @Test
    fun validate_Twice_on_Sametime() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = {
                delay(1.seconds)
                "DATA"
            },
        )
        launch {
            swrStore.validate()
        }
        launch {
            swrStore.validate()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            expectNoEvents()
        }
    }

    @Test
    fun flow_on_different_instance() = runTest {
        val key = "KEY"
        val fetcher = { _: String -> "DATA" }
        val swrStore1 = SWRStore(
            key = key,
            fetcher = fetcher,
        )
        val swrStore2 = SWRStore(
            key = key,
            fetcher = fetcher,
        )
        launch {
            swrStore1.flow.test {
                awaitItem().apply {
                    assertIs<SWRStoreState.Loading<String>>(this)
                    assertEquals(null, data)
                }
                awaitItem().apply {
                    assertIs<SWRStoreState.Completed<String>>(this)
                    assertEquals("DATA", data)
                }
                expectNoEvents()
            }
        }
        launch {
            swrStore2.flow.test {
                awaitItem().apply {
                    assertIs<SWRStoreState.Loading<String>>(this)
                    assertEquals(null, data)
                }
                awaitItem().apply {
                    assertIs<SWRStoreState.Completed<String>>(this)
                    assertEquals("DATA", data)
                }
                expectNoEvents()
            }
        }
        launch {
            swrStore1.validate()
        }
    }

    @Test
    fun get_Both_Success_Remote() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        val actual = swrStore.get(from = GettingFrom.Both)
        assertEquals("DATA", actual.getOrNull())
    }

    @Test
    fun get_Both_Success_Cache() = runTest {
        val swrStore = SWRStore<String, String>(
            key = "KEY",
            fetcher = { throw NoSuchElementException() },
        )
        swrStore.update("CACHE")
        val actual = swrStore.get(from = GettingFrom.Both)
        assertEquals("CACHE", actual.getOrNull())
    }

    @Test
    fun get_Both_Failed() = runTest {
        val swrStore = SWRStore<String, String>(
            key = "KEY",
            fetcher = { throw NoSuchElementException() },
        )
        val actual = swrStore.get(from = GettingFrom.Both)
        assertIs<NoSuchElementException>(actual.exceptionOrNull())
    }

    @Test
    fun get_Local_Success() = runTest {
        val swrStore = SWRStore<String, String>(
            key = "KEY",
            fetcher = { throw NoSuchElementException() },
        )
        swrStore.update("CACHE")
        val actual = swrStore.get(from = GettingFrom.LocalOnly)
        assertEquals("CACHE", actual.getOrNull())
    }

    @Test
    fun get_Local_Failed() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        val actual = swrStore.get(from = GettingFrom.LocalOnly)
        assertIs<NoSuchElementException>(actual.exceptionOrNull())
    }

    @Test
    fun get_Remote_Success() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        val actual = swrStore.get(from = GettingFrom.RemoteOnly)
        assertEquals("DATA", actual.getOrNull())
    }

    @Test
    fun get_Remote_Failed() = runTest {
        val swrStore = SWRStore<String, String>(
            key = "KEY",
            fetcher = { throw NoSuchElementException() },
        )
        swrStore.update("CACHE")
        val actual = swrStore.get(from = GettingFrom.RemoteOnly)
        assertIs<NoSuchElementException>(actual.exceptionOrNull())
    }

    @Test
    fun refresh_Success() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        launch {
            swrStore.refresh()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            expectNoEvents()
        }
    }

    @Test
    fun refresh_Success_Success() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        launch {
            swrStore.refresh()
            swrStore.refresh()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            expectNoEvents()
        }
    }

    @Test
    fun refresh_Error_Success() = runTest {
        var fetcher: (key: String) -> String = { throw NoSuchElementException() }
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { fetcher(it) },
        )
        launch {
            swrStore.refresh()
            fetcher = { "DATA" }
            swrStore.refresh()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Error<String>>(this)
                assertEquals(null, data)
                assertIs<NoSuchElementException>(error)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            expectNoEvents()
        }
    }

    @Test
    fun refresh_Success_Error() = runTest {
        var fetcher: (key: String) -> String = { "DATA" }
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { fetcher(it) },
        )
        launch {
            swrStore.refresh()
            fetcher = { throw NoSuchElementException() }
            swrStore.refresh()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Error<String>>(this)
                assertEquals(null, data)
                assertIs<NoSuchElementException>(error)
            }
            expectNoEvents()
        }
    }

    @Test
    fun update() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        launch {
            swrStore.validate()
            swrStore.update("UPDATE")
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("UPDATE", data)
            }
            expectNoEvents()
        }
    }

    @Test
    fun clear() = runTest {
        val swrStore = SWRStore(
            key = "KEY",
            fetcher = { "DATA" },
        )
        launch {
            swrStore.validate()
            swrStore.clear()
        }
        swrStore.flow.test {
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Completed<String>>(this)
                assertEquals("DATA", data)
            }
            awaitItem().apply {
                assertIs<SWRStoreState.Loading<String>>(this)
                assertEquals(null, data)
            }
            expectNoEvents()
        }
    }
}
