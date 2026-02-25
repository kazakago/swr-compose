package com.kazakago.swr.compose

import androidx.compose.runtime.Immutable
import com.kazakago.swr.runtime.SWRTrigger

@Immutable
public data class SWRMutationState<KEY : Any, DATA, ARG>(
    val trigger: SWRTrigger<KEY, DATA, ARG>,
    val isMutating: Boolean,
    val data: DATA?,
    val error: Throwable?,
    val reset: () -> Unit,
)
