package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWRInfinite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.example.generated.resources.Res
import swr.example.generated.resources.arrow_back_24dp
import swr.example.generated.resources.refresh_24dp
import kotlin.time.Duration.Companion.seconds

private val fetcher: suspend (key: String) -> List<String> = { key ->
    delay(1.seconds)
    List(10) { "$key - $it" }
}

private val getKey: (pageIndex: Int, previousPageData: List<String>?) -> String? = { pageIndex, previousPageData ->
    if (previousPageData != null && previousPageData.isEmpty()) null
    else "/infinite_paginationKey/$pageIndex"
}

@SerialName("infinite_pagination")
@Serializable
data object InfinitePaginationRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InfinitePaginationScreen(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val (data, error, isValidating, isLoading, mutate, size, setSize) = rememberSWRInfinite(getKey, fetcher) {
        initialSize = 2              // default is 1
//        revalidateAll = false      // default is false
//        revalidateFirstPage = true // default is true
//        persistSize = false        // default is false
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Infinite Pagination") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.arrow_back_24dp), contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { mutate() } }) {
                        Icon(painterResource(Res.drawable.refresh_24dp), contentDescription = null)
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (data == null) {
                CircularProgressIndicator()
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(data.size) { index ->
                        InfinitePaginationRow(data[index])
                    }
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            OutlinedButton(onClick = { setSize(size + 1) }) {
                                Text("Load More")
                            }
                            Spacer(Modifier.size(8.dp))
                            Text("${data.flatMap { it ?: emptyList() }.size} items listed")
                            Spacer(Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfinitePaginationRow(page: List<String>?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (page == null) {
            CircularProgressIndicator()
        } else {
            page.forEach { content ->
                Text(content, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview
@Composable
fun InfinitePaginationScreenPreview() {
    MaterialTheme {
        InfinitePaginationScreen(
            onBack = {},
        )
    }
}
