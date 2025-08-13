package com.kazakago.swr.example.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import com.kazakago.swr.example.ui.LoadingContent
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.seconds

private val userFetcher: suspend (key: String) -> String = {
    delay(1.seconds)
    "User1"
}

private val projectsFetcher: suspend (key: String) -> List<String> = {
    delay(1.seconds)
    listOf("Project1", "Project2", "Project3")
}

@SerialName("conditional_fetching")
@Serializable
data object ConditionalFetchingRoute

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConditionalFetchingScreen(
    onBack: () -> Unit,
) {
    val (user, _, _) = rememberSWR(key = "/confidential_fetching/user", fetcher = userFetcher)
    val (projects, _, _) = rememberSWR(key = if (user != null) "/confidential_fetching/$user/projects" else null, fetcher = projectsFetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conditional Fetching") },
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
            if (user == null) {
                LoadingContent()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                ) {
                    Text(user)
                    if (projects == null) {
                        CircularProgressIndicator()
                    } else {
                        Text(projects.joinToString(", "))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ConditionalFetchingScreenPreview() {
    MaterialTheme {
        ConditionalFetchingScreen(
            onBack = {},
        )
    }
}
