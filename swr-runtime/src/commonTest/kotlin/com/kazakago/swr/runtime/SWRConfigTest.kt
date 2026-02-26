package com.kazakago.swr.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class SWRConfigTest {

    @Test
    fun copyConstructorCopiesAllFields() {
        val original = SWRConfig<Any, Any>().apply {
            revalidateIfStale = false
            revalidateOnMount = true
            revalidateOnFocus = false
            revalidateOnReconnect = false
            refreshInterval = 10.seconds
            refreshWhenHidden = true
            refreshWhenOffline = true
            shouldRetryOnError = false
            dedupingInterval = 7.seconds
            focusThrottleInterval = 15.seconds
            loadingTimeout = 8.seconds
            errorRetryInterval = 12.seconds
            errorRetryCount = 5
            initialSize = 3
            revalidateAll = true
            revalidateFirstPage = false
            persistSize = true
            keepPreviousData = true
            isPaused = { true }
        }

        val copied = SWRConfig<String, String>(original)

        assertEquals(original.revalidateIfStale, copied.revalidateIfStale)
        assertEquals(original.revalidateOnMount, copied.revalidateOnMount)
        assertEquals(original.revalidateOnFocus, copied.revalidateOnFocus)
        assertEquals(original.revalidateOnReconnect, copied.revalidateOnReconnect)
        assertEquals(original.refreshInterval, copied.refreshInterval)
        assertEquals(original.refreshWhenHidden, copied.refreshWhenHidden)
        assertEquals(original.refreshWhenOffline, copied.refreshWhenOffline)
        assertEquals(original.shouldRetryOnError, copied.shouldRetryOnError)
        assertEquals(original.dedupingInterval, copied.dedupingInterval)
        assertEquals(original.focusThrottleInterval, copied.focusThrottleInterval)
        assertEquals(original.loadingTimeout, copied.loadingTimeout)
        assertEquals(original.errorRetryInterval, copied.errorRetryInterval)
        assertEquals(original.errorRetryCount, copied.errorRetryCount)
        assertEquals(original.initialSize, copied.initialSize)
        assertEquals(original.revalidateAll, copied.revalidateAll)
        assertEquals(original.revalidateFirstPage, copied.revalidateFirstPage)
        assertEquals(original.persistSize, copied.persistSize)
        assertEquals(original.keepPreviousData, copied.keepPreviousData)
        assertEquals(original.isPaused, copied.isPaused)
        // fallbackData is intentionally not copied (always null after copy)
        assertNull(copied.fallbackData)
    }

    @Test
    fun copyConstructorPreservesDefaults() {
        val original = SWRConfig<Any, Any>()
        val copied = SWRConfig<String, String>(original)

        assertEquals(true, copied.revalidateIfStale)
        assertNull(copied.revalidateOnMount)
        assertEquals(true, copied.revalidateOnFocus)
        assertEquals(true, copied.revalidateOnReconnect)
        assertEquals(0.seconds, copied.refreshInterval)
        assertEquals(false, copied.refreshWhenHidden)
        assertEquals(false, copied.refreshWhenOffline)
        assertEquals(true, copied.shouldRetryOnError)
        assertEquals(2.seconds, copied.dedupingInterval)
        assertEquals(5.seconds, copied.focusThrottleInterval)
        assertEquals(3.seconds, copied.loadingTimeout)
        assertEquals(5.seconds, copied.errorRetryInterval)
        assertNull(copied.errorRetryCount)
        assertNull(copied.fallbackData)
        assertEquals(1, copied.initialSize)
        assertEquals(false, copied.revalidateAll)
        assertEquals(true, copied.revalidateFirstPage)
        assertEquals(false, copied.persistSize)
        assertEquals(false, copied.keepPreviousData)
        assertNull(copied.isPaused)
    }

    @Test
    fun copyConstructorThenApplyOverrides() {
        val global = SWRConfig<Any, Any>().apply {
            dedupingInterval = 10.seconds
            errorRetryCount = 3
        }

        val local = SWRConfig<String, String>(global).apply {
            errorRetryCount = 5
        }

        // dedupingInterval should be inherited from global
        assertEquals(10.seconds, local.dedupingInterval)
        // errorRetryCount should be overridden by local apply
        assertEquals(5, local.errorRetryCount)
    }
}
