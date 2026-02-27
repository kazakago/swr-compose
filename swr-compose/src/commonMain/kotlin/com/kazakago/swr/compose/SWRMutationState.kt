package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRTrigger

/**
 * Compose state returned by [rememberSWRMutation].
 *
 * Equivalent to the return value of React SWR's `useSWRMutation`.
 */
@Immutable
public data class SWRMutationState<KEY : Any, DATA, ARG>(
    /** Call this to trigger the mutation. */
    val trigger: SWRTrigger<KEY, DATA, ARG>,

    /** `true` while a mutation is in progress. */
    val isMutating: Boolean,

    /** The data returned by the last successful mutation, or `null` if none has occurred. */
    val data: DATA?,

    /** The error thrown by the last failed mutation, or `null` if none has occurred. */
    val error: Throwable?,

    /** Resets [data], [error], and [isMutating] to their initial values. */
    val reset: () -> Unit,
)
