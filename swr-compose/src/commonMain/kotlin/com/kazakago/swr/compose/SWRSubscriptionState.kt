package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable

@Immutable
public data class SWRSubscriptionState<DATA>(
    val data: DATA?,
    val error: Throwable?,
)
