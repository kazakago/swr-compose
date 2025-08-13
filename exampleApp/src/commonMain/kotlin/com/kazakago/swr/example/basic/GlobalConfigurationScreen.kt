package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.kazakago.swr.compose.SWRConfig
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    "Hello world!"
}

@SerialName("global_configuration")
@Serializable
data object GlobalConfigurationRoute

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GlobalConfigurationScreen(
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    SWRConfig(config = {
        onSuccess = { _, _, _ ->
            scope.launch { snackbarHostState.showSnackbar("1st Global SWRConfig") }
        }
    }) {
        val (events) = rememberSWR(key = "/global_configuration/events", fetcher = fetcher)
        SWRConfig(config = {
            onSuccess = { _, _, _ ->
                scope.launch { snackbarHostState.showSnackbar("2nd Global SWRConfig") }
            }
        }) {
            val (projects) = rememberSWR(key = "/global_configuration/projects", fetcher = fetcher)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Global Configuration") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                    if (events == null || projects == null) {
                        LoadingContent()
                    } else {
                        Column {
                            Text(events)
                            Text(projects)
                        }
                    }
                }
            }
        }
    }
}
