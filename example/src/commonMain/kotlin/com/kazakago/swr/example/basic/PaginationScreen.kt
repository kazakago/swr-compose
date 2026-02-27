package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import com.kazakago.swr.compose.rememberSWR
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import swr.example.generated.resources.Res
import swr.example.generated.resources.arrow_back_24dp
import kotlin.time.Duration.Companion.seconds

private val fetcher: suspend (key: String) -> List<String> = { key ->
    delay(1.seconds)
    List(10) { "$key - $it" }
}

@SerialName("pagination")
@Serializable
data object PaginationRoute : NavKey

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PaginationScreen(
    onBack: () -> Unit,
) {
    var pageCount by rememberSaveable { mutableIntStateOf(1) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagination") },
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
            LazyColumn(Modifier.fillMaxSize()) {
                items(pageCount) { page ->
                    PaginationRow(page = page)
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        OutlinedButton(onClick = { pageCount += 1 }) {
                            Text("Load More")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationRow(page: Int) {
    val (list) = rememberSWR(key = "/pagination/$page", fetcher = fetcher)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (list == null) {
            CircularProgressIndicator()
        } else {
            list.forEach { content ->
                Text(content, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview
@Composable
fun PaginationScreenPreview() {
    MaterialTheme {
        PaginationScreen(
            onBack = {},
        )
    }
}
