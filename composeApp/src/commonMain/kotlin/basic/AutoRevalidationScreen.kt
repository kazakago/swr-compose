package basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import ui.LoadingContent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    Clock.System.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AutoRevalidationScreen(navController: NavController) {
    val (data, _, _, isValidating) = useSWR(key = "/auto_revalidation", fetcher = fetcher) {
        revalidateOnMount = true     // default is null
        revalidateIfStale = true     // default is true
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
                    IconButton(onClick = navController::popBackStack) {
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
            contentAlignment = Alignment.TopCenter,
        ) {
            if (data == null) {
                LoadingContent()
            } else {
                if (isValidating) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(data)
                }
            }
        }
    }
}
