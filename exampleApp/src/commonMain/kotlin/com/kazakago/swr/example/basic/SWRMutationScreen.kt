package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWRMutation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.exampleapp.generated.resources.Res
import swr.exampleapp.generated.resources.arrow_back_24dp

private val swrMutationFetcher: suspend (key: String, arg: String) -> String = { _, arg ->
    delay(1000)
    "Posted: $arg"
}

@SerialName("swr_mutation")
@Serializable
data object SWRMutationRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SWRMutationScreen(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val (trigger, isMutating, data, error, reset) = rememberSWRMutation<String, String, String>(
        key = "/swr-mutation",
        fetcher = swrMutationFetcher,
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Mutation") },
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
            Text(text = "data: ${data ?: "(none)"}")
            Spacer(Modifier.size(8.dp))
            Text(text = "error: ${error?.message ?: "(none)"}")
            Spacer(Modifier.size(8.dp))
            Text(text = "isMutating: $isMutating")
            Spacer(Modifier.size(16.dp))
            Button(
                enabled = !isMutating,
                onClick = {
                    scope.launch {
                        trigger("hello world")
                    }
                },
            ) {
                Text("trigger")
            }
            Spacer(Modifier.size(8.dp))
            Button(onClick = reset) {
                Text("reset")
            }
        }
    }
}
