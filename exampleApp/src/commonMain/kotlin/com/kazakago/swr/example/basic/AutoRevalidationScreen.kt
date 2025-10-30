package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.delay
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import swr.exampleapp.generated.resources.Res
import swr.exampleapp.generated.resources.arrow_back_24dp
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    Clock.System.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
}

@SerialName("auto_revalidation")
@Serializable
data object AutoRevalidationRoute

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AutoRevalidationScreen(
    onBack: () -> Unit,
) {
    val (data, _, isLoading) = rememberSWR(key = "/auto_revalidation", fetcher = fetcher) {
        revalidateOnMount = true     // default is null
        revalidateOnFocus = true     // default is true
        revalidateOnReconnect = true // default is true
        refreshInterval = 10.seconds // default is 0.seconds (=disable)
        refreshWhenHidden = false    // default is false
        refreshWhenOffline = false   // default is false
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Revalidation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.arrow_back_24dp), contentDescription = null)
                    }
                },
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Companion.TopCenter,
        ) {
            if (data == null) {
                LoadingContent()
            } else {
                if (isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(data)
                }
            }
        }
    }
}

@Preview
@Composable
fun AutoRevalidationScreenPreview() {
    MaterialTheme {
        AutoRevalidationScreen(
            onBack = {},
        )
    }
}
