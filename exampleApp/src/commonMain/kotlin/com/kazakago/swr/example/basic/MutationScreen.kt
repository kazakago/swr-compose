package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.exampleapp.generated.resources.Res
import swr.exampleapp.generated.resources.arrow_back_24dp

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    "Fetched Data"
}

private val mutator: suspend () -> String = {
    delay(1000)
    "Mutated Data"
}

@SerialName("mutation")
@Serializable
data object MutationRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MutationScreen(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val (data, _, _, _, mutate) = rememberSWR(key = "/mutation", fetcher = fetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mutate") },
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(data)
                    Row {
                        Button(onClick = {
                            scope.launch {
                                mutate(data = mutator) {
                                    optimisticData = "Optimistic Data" // default is null
                                    revalidate = false                 // default is true
                                    populateCache = true               // default is true
                                    rollbackOnError = true             // default is true
                                }
                            }
                        }) {
                            Text("mutate")
                        }
                    }
                }
            }
        }
    }
}
