package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.exampleapp.generated.resources.Res
import swr.exampleapp.generated.resources.arrow_back_24dp
import kotlin.time.Duration.Companion.seconds

private val fetcher: suspend (key: String) -> String = { key ->
    delay(1.seconds)
    "Argument is '$key'"
}

@SerialName("arguments")
@Serializable
data object ArgumentsRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ArgumentsScreen(
    onBack: () -> Unit,
) {
    val (data1) = rememberSWR(key = "/arguments/google", fetcher = fetcher)
    val (data2) = rememberSWR(key = "/arguments/apple", fetcher = fetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arguments") },
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
            contentAlignment = Alignment.Companion.Center,
        ) {
            if (data1 == null || data2 == null) {
                LoadingContent()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                ) {
                    Text(data1)
                    Text(data2)
                }
            }
        }
    }
}

@Preview
@Composable
fun ArgumentsScreenPreview() {
    MaterialTheme {
        ArgumentsScreen(
            onBack = {},
        )
    }
}
