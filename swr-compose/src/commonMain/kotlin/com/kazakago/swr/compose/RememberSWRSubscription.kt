package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazakago.swr.runtime.SWRSubscription
import com.kazakago.swr.store.persister.Persister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Composable
public fun <KEY : Any, DATA> rememberSWRSubscription(
    key: KEY?,
    scope: CoroutineScope = rememberCoroutineScope(),
    persister: Persister<KEY, DATA>? = null,
    subscribe: suspend (key: KEY) -> Flow<DATA>,
): SWRSubscriptionState<DATA> {
    return if (!LocalInspectionMode.current) {
        val cacheOwner = LocalSWRCacheOwner.current

        val subscription = remember(key) {
            SWRSubscription(
                key = key,
                scope = scope,
                persister = persister,
                cacheOwner = cacheOwner,
                subscribe = subscribe,
            )
        }

        // Cancel the previous subscription only when key changes, not on composable leave.
        // SideEffect fires after each successful composition but NOT when the composable
        // leaves the tree, so an external scope (e.g. viewModelScope) can keep the job
        // alive across screen transitions.
        val previousRef = remember { arrayOf(subscription) }
        SideEffect {
            if (previousRef[0] !== subscription) {
                previousRef[0].cancel()
                previousRef[0] = subscription
            }
        }

        val storeState by subscription.stateFlow.collectAsStateWithLifecycle()
        val error by subscription.error.collectAsStateWithLifecycle()

        SWRSubscriptionState(
            data = storeState.data,
            error = error,
        )
    } else {
        SWRSubscriptionState(
            data = null,
            error = null,
        )
    }
}
