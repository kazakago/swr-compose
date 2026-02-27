package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWRSubscription
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.example.generated.resources.Res
import swr.example.generated.resources.arrow_back_24dp

@SerialName("subscription")
@Serializable
data object SubscriptionRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SubscriptionScreen(
    onBack: () -> Unit,
) {
    val (data, error) = rememberSWRSubscription(
        key = "/subscription",
        subscribe = { _ ->
            flow {
                var count = 0
                while (true) {
                    delay(1000L)
                    count++
                    emit(count)
                }
            }
        },
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.arrow_back_24dp), contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Count: ${data ?: "..."}",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.size(8.dp))
            Text(text = "error: ${error?.message ?: "(none)"}")
        }
    }
}

@Preview
@Composable
fun SubscriptionScreenPreview() {
    MaterialTheme {
        SubscriptionScreen(
            onBack = {},
        )
    }
}
