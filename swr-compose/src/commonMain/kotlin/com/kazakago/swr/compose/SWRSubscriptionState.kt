package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable

/**
 * Compose state returned by [rememberSWRSubscription].
 *
 * Equivalent to the return value of React SWR's `useSWRSubscription`.
 */
@Immutable
public data class SWRSubscriptionState<DATA>(
    /** The most recently emitted value from the subscription, or `null` if none has arrived yet. */
    val data: DATA?,

    /** The error thrown by the subscription, or `null` if no error has occurred. */
    val error: Throwable?,
)
