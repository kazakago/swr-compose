package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.compose.rememberSWRPreload
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.example.generated.resources.Res
import swr.example.generated.resources.arrow_back_24dp

private val fetcher: suspend (key: String) -> String = {
    delay(3000)
    "Hello world!"
}

@SerialName("prefetching")
@Serializable
data object PrefetchingRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrefetchingScreen(
    onBack: () -> Unit,
    toNext: () -> Unit,
    scope: CoroutineScope,
) {
    val preload = rememberSWRPreload(key = "/prefetching", fetcher = fetcher, scope = scope)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prefetching") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.arrow_back_24dp), contentDescription = null)
                    }
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { scope.launch { preload() } }) {
                Text("Start prefetch (3s)")
            }
            Spacer(Modifier.size(16.dp))
            Button(onClick = toNext) {
                Text("Move to next Screen")
            }
        }
    }
}

@SerialName("prefetching_next")
@Serializable
data object PrefetchingNextRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrefetchingNextScreen(
    onBack: () -> Unit,
    scope: CoroutineScope,
) {
    val (data) = rememberSWR(key = "/prefetching", fetcher = fetcher, scope = scope)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prefetching Next") },
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
            contentAlignment = Alignment.Center,
        ) {
            if (data == null) {
                LoadingContent()
            } else {
                Text(data)
            }
        }
    }
}
