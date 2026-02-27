package com.kazakago.swr.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.LocalSWRCacheOwner
import com.kazakago.swr.example.todolist.server.MockServer
import com.kazakago.swr.example.todolist.server.MockServerAllFailed
import com.kazakago.swr.example.todolist.server.MockServerLoadingSlow
import com.kazakago.swr.example.todolist.server.MockServerMutationFailed
import com.kazakago.swr.example.todolist.server.MockServerRandomFailed
import com.kazakago.swr.example.todolist.server.MockServerSucceed
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("main")
@Serializable
data object MainRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    mockServer: MutableState<MockServer>,
    isClearCache: MutableState<Boolean>,
    moveToDataFetching: () -> Unit,
    moveToGlobalConfiguration: () -> Unit,
    moveToErrorHandling: () -> Unit,
    moveToAutoRevalidation: () -> Unit,
    moveToConditionalFetching: () -> Unit,
    moveToArguments: () -> Unit,
    moveToMutation: () -> Unit,
    moveToSWRMutation: () -> Unit,
    moveToPagination: () -> Unit,
    moveToInfinitePagination: () -> Unit,
    moveToPrefetching: () -> Unit,
    moveToTodoList: () -> Unit,
) {
    val swrCacheOwner = LocalSWRCacheOwner.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SWR Compose Example") })
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Clear cache before transition", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.size(16.dp))
                    Switch(checked = isClearCache.value, onCheckedChange = { isClearCache.value = !isClearCache.value })
                }
                Spacer(Modifier.size(8.dp))
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Basic Example", style = MaterialTheme.typography.titleLarge)
                        ExampleButton("Data Fetching") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToDataFetching()
                        }
                        ExampleButton("Global Configuration") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToGlobalConfiguration()
                        }
                        ExampleButton("Error Handling") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToErrorHandling()
                        }
                        ExampleButton("Auto Revalidation") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToAutoRevalidation()
                        }
                        ExampleButton("Conditional Fetching") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToConditionalFetching()
                        }
                        ExampleButton("Arguments") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToArguments()
                        }
                        ExampleButton("Mutate") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToMutation()
                        }
                        ExampleButton("Remote Mutation") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToSWRMutation()
                        }
                        ExampleButton("Pagination") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToPagination()
                        }
                        ExampleButton("Infinite Pagination") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToInfinitePagination()
                        }
                        ExampleButton("Prefetching") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            moveToPrefetching()
                        }
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "ToDo List Example", style = MaterialTheme.typography.titleLarge)
                        ExampleButton("with All Succeed") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            mockServer.value = MockServerSucceed
                            moveToTodoList()
                        }
                        ExampleButton("with All Failed") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            mockServer.value = MockServerAllFailed
                            moveToTodoList()
                        }
                        ExampleButton("with Mutation Failed") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            mockServer.value = MockServerMutationFailed
                            moveToTodoList()
                        }
                        ExampleButton("with Random Failed") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            mockServer.value = MockServerRandomFailed
                            moveToTodoList()
                        }
                        ExampleButton("with Loading Slow") {
                            if (isClearCache.value) {
                                swrCacheOwner.clearAll()
                            }
                            mockServer.value = MockServerLoadingSlow
                            moveToTodoList()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExampleButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen(
            mockServer = remember { mutableStateOf(MockServerSucceed) },
            isClearCache = remember { mutableStateOf(true) },
            moveToDataFetching = {},
            moveToGlobalConfiguration = {},
            moveToErrorHandling = {},
            moveToAutoRevalidation = {},
            moveToConditionalFetching = {},
            moveToArguments = {},
            moveToMutation = {},
            moveToSWRMutation = {},
            moveToPagination = {},
            moveToInfinitePagination = {},
            moveToPrefetching = {},
            moveToTodoList = {},
        )
    }
}
