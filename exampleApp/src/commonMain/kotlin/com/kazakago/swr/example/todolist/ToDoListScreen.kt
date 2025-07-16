package com.kazakago.swr.example.todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazakago.swr.compose.rememberSWR
import com.kazakago.swr.example.todolist.server.LocalMockServer
import com.kazakago.swr.example.ui.ErrorContent
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Serializable
data object ToDoListRoute

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ToDoListScreen(
    onBack: () -> Unit,
) {
    val mockServer = LocalMockServer.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isMuting = remember { mutableStateOf(false) }
    val openToDoCreationDialog = remember { mutableStateOf(false) }
    val openToDoEditingDialog = remember { mutableStateOf<Pair<Int, String>?>(null) }

    val (todoList, error, isLoading, mutate) = rememberSWR(
        key = "/get_todos/$mockServer",
        fetcher = { mockServer.getToDoList() },
    ) {
        onLoadingSlow = { _, _ ->
            scope.launch {
                snackbarHostState.showSnackbar("Loading is slow, Please wait..")
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ToDo List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { mutate() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openToDoCreationDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue),
        ) {
            if (todoList == null) {
                if (isLoading) {
                    LoadingContent()
                } else if (error != null) {
                    ErrorContent { scope.launch { mutate() } }
                }
            } else {
                if (isLoading || isMuting.value) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                ) {
                    items(todoList.size) { index ->
                        ToDoRow(index, todoList[index]) {
                            openToDoEditingDialog.value = index to it
                        }
                    }
                }
                if (openToDoCreationDialog.value) {
                    ToDoCreationDialog(
                        onSubmit = { text ->
                            openToDoCreationDialog.value = false
                            scope.launch {
                                isMuting.value = true
                                mutate(data = { mockServer.addToDo(text) }) {
                                    revalidate = false
                                    optimisticData = todoList.toMutableList().apply {
                                        add(text)
                                    }
                                }.onFailure {
                                    launch { snackbarHostState.showSnackbar("Failed, Data was rollback.") }
                                }
                                isMuting.value = false
                            }
                        },
                        onCancel = {
                            openToDoCreationDialog.value = false
                        },
                    )
                }
                openToDoEditingDialog.value?.let { (index, value) ->
                    ToDoEditingDialog(
                        initialText = value,
                        onSubmit = { text ->
                            openToDoEditingDialog.value = null
                            scope.launch {
                                isMuting.value = true
                                mutate(data = { mockServer.editToDo(index, text) }) {
                                    revalidate = false
                                    optimisticData = todoList.toMutableList().apply {
                                        set(index, text)
                                    }
                                }.onFailure {
                                    launch { snackbarHostState.showSnackbar("Error, Data was rollback.") }
                                }
                                isMuting.value = false
                            }
                        },
                        onDelete = {
                            openToDoEditingDialog.value = null
                            scope.launch {
                                isMuting.value = true
                                mutate(data = { mockServer.removeToDo(index) }) {
                                    revalidate = false
                                    optimisticData = todoList.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }.onFailure {
                                    launch { snackbarHostState.showSnackbar("Error, Data was rollback.") }
                                }
                                isMuting.value = false
                            }
                        },
                        onCancel = {
                            openToDoEditingDialog.value = null
                        },
                    )
                }
            }
        }
        LaunchedEffect(error) {
            if (error != null) {
                snackbarHostState.showSnackbar("Validation failed.")
            }
        }
    }
}

@Composable
private fun ToDoRow(
    index: Int,
    value: String,
    onClick: (value: String) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onClick(value) },
    ) {
        Row(Modifier.padding(16.dp)) {
            Text(text = "$index:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.size(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Preview
@Composable
fun PreviewToDoListScreen() {
    MaterialTheme {
        ToDoListScreen(
            onBack = {},
        )
    }
}
