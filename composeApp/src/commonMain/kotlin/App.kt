import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import basic.ArgumentsScreen
import basic.AutoRevalidationScreen
import basic.ConditionalFetchingScreen
import basic.DataFetchingScreen
import basic.ErrorHandlingScreen
import basic.GlobalConfigurationScreen
import basic.InfinitePaginationScreen
import basic.MutationScreen
import basic.PaginationScreen
import basic.PrefetchingNextScreen
import basic.PrefetchingScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import todolist.ToDoListScreen
import todolist.server.LocalMockServer
import todolist.server.MockServer
import todolist.server.MockServerSucceed
import ui.theme.AppTheme

@Composable
@Preview
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {},
) {
    AppTheme {
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val mockServer = remember { mutableStateOf<MockServer>(MockServerSucceed) }
        val isClearCache = remember { mutableStateOf(true) }
        LaunchedEffect(navController) {
            onNavHostReady(navController)
        }
        CompositionLocalProvider(LocalMockServer provides mockServer.value) {
            NavHost(navController = navController, startDestination = "main") {
                composable("main") { MainScreen(navController, mockServer, isClearCache) }
                composable("data_fetching") { DataFetchingScreen(navController) }
                composable("global_configuration") { GlobalConfigurationScreen(navController) }
                composable("error_handling") { ErrorHandlingScreen(navController) }
                composable("auto_revalidation") { AutoRevalidationScreen(navController) }
                composable("conditional_fetching") { ConditionalFetchingScreen(navController) }
                composable("arguments") { ArgumentsScreen(navController) }
                composable("mutation") { MutationScreen(navController) }
                composable("todolist") { ToDoListScreen(navController) }
                composable("pagination") { PaginationScreen(navController) }
                composable("infinite_pagination") { InfinitePaginationScreen(navController) }
                composable("prefetching") { PrefetchingScreen(navController, scope) }
                composable("prefetching_next") { PrefetchingNextScreen(navController, scope) }
            }
        }
    }
}
