package com.kazakago.swr.store

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SWRStoreStateTest {

    @Test
    fun doAction_Completed() {
        val state = SWRStoreState.Completed(10)
        assertEquals(10, state.data)
        assertEquals(false, state.isValidating)
        assertEquals(null, state.error)
    }

    @Test
    fun doAction_Loading() {
        val state = SWRStoreState.Loading(10)
        assertEquals(10, state.data)
        assertEquals(true, state.isValidating)
        assertEquals(null, state.error)
    }

    @Test
    fun doAction_Error() {
        val state = SWRStoreState.Error(10, IllegalStateException())
        assertEquals(10, state.data)
        assertEquals(false, state.isValidating)
        assertIs<IllegalStateException>(state.error)
    }
}
