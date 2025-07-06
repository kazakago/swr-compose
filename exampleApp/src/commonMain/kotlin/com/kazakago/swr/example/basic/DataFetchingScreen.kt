package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.example.ui.ErrorContent
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    "Hello world!"
}

@Serializable
data object DataFetchingRoute

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DataFetchingScreen(
    onBack: () -> Unit,
) {
    val (data, error) = rememberSWR(key = "/data_fetching", fetcher = fetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Fetching") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Companion.Center,
        ) {
            if (error != null) {
                ErrorContent()
            } else if (data == null) {
                LoadingContent()
            } else {
                Text(data)
            }
        }
    }
}

@Preview
@Composable
fun DataFetchingScreenPreview() {
    MaterialTheme {
        DataFetchingScreen(
            onBack = {},
        )
    }
}
