package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.example.ui.ErrorContent
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.exampleapp.generated.resources.Res
import swr.exampleapp.generated.resources.arrow_back_24dp
import kotlin.time.Duration.Companion.seconds

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    throw NullPointerException()
}

@SerialName("error_handling")
@Serializable
data object ErrorHandlingRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ErrorHandlingScreen(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val (data, error, _, mutate) = rememberSWR(key = "/error_handling", fetcher = fetcher) {
        shouldRetryOnError = true           // default is true
        errorRetryCount = 3                 // default is null
        errorRetryInterval = 5.seconds      // default is 5.seconds
        onError = { error, key, config ->   // default is null
            scope.launch {
                snackbarHostState.showSnackbar(error.toString())
            }
        }
        onErrorRetry = { _, _, _, _, _ -> // default is `com.kazakago.compose.swr.retry.SWRRetryDefault.kt`
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Error Handling") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.arrow_back_24dp), contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            if (error != null) {
                ErrorContent { scope.launch { mutate() } }
            } else if (data == null) {
                LoadingContent()
            } else {
                Text(data)
            }
        }
    }
}
