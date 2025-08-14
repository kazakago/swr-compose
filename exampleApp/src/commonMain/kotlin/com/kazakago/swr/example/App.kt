package com.kazakago.swr.example

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
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
import com.kazakago.swr.example.basic.ArgumentsRoute
import com.kazakago.swr.example.basic.ArgumentsScreen
import com.kazakago.swr.example.basic.AutoRevalidationRoute
import com.kazakago.swr.example.basic.AutoRevalidationScreen
import com.kazakago.swr.example.basic.ConditionalFetchingRoute
import com.kazakago.swr.example.basic.ConditionalFetchingScreen
import com.kazakago.swr.example.basic.DataFetchingRoute
import com.kazakago.swr.example.basic.DataFetchingScreen
import com.kazakago.swr.example.basic.ErrorHandlingRoute
import com.kazakago.swr.example.basic.ErrorHandlingScreen
import com.kazakago.swr.example.basic.GlobalConfigurationRoute
import com.kazakago.swr.example.basic.GlobalConfigurationScreen
import com.kazakago.swr.example.basic.InfinitePaginationRoute
import com.kazakago.swr.example.basic.InfinitePaginationScreen
import com.kazakago.swr.example.basic.MutationRoute
import com.kazakago.swr.example.basic.MutationScreen
import com.kazakago.swr.example.basic.PaginationRoute
import com.kazakago.swr.example.basic.PaginationScreen
import com.kazakago.swr.example.basic.PrefetchingNextRoute
import com.kazakago.swr.example.basic.PrefetchingNextScreen
import com.kazakago.swr.example.basic.PrefetchingRoute
import com.kazakago.swr.example.basic.PrefetchingScreen
import com.kazakago.swr.example.todolist.ToDoListRoute
import com.kazakago.swr.example.todolist.ToDoListScreen
import com.kazakago.swr.example.todolist.server.LocalMockServer
import com.kazakago.swr.example.todolist.server.MockServer
import com.kazakago.swr.example.todolist.server.MockServerSucceed

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {},
) = MaterialExpressiveTheme {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val mockServer = remember { mutableStateOf<MockServer>(MockServerSucceed) }
    val isClearCache = remember { mutableStateOf(true) }
    LaunchedEffect(navController) {
        onNavHostReady(navController)
    }
    CompositionLocalProvider(LocalMockServer provides mockServer.value) {
        NavHost(
            navController = navController,
            startDestination = MainRoute,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 100 }) + fadeIn()
            },
            exitTransition = {
                ExitTransition.None
            },
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 100 }) + fadeOut()
            },
        ) {
            composable<MainRoute> {
                MainScreen(
                    mockServer = mockServer,
                    isClearCache = isClearCache,
                    moveToDataFetching = { navController.navigate(DataFetchingRoute) },
                    moveToGlobalConfiguration = { navController.navigate(GlobalConfigurationRoute) },
                    moveToErrorHandling = { navController.navigate(ErrorHandlingRoute) },
                    moveToAutoRevalidation = { navController.navigate(AutoRevalidationRoute) },
                    moveToConditionalFetching = { navController.navigate(ConditionalFetchingRoute) },
                    moveToArguments = { navController.navigate(ArgumentsRoute) },
                    moveToMutation = { navController.navigate(MutationRoute) },
                    moveToPagination = { navController.navigate(PaginationRoute) },
                    moveToInfinitePagination = { navController.navigate(InfinitePaginationRoute) },
                    moveToPrefetching = { navController.navigate(PrefetchingRoute) },
                    moveToTodoList = { navController.navigate(ToDoListRoute) },
                )
            }
            composable<DataFetchingRoute> {
                DataFetchingScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<GlobalConfigurationRoute> {
                GlobalConfigurationScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<ErrorHandlingRoute> {
                ErrorHandlingScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<AutoRevalidationRoute> {
                AutoRevalidationScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<ConditionalFetchingRoute> {
                ConditionalFetchingScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<ArgumentsRoute> {
                ArgumentsScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<MutationRoute> {
                MutationScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<ToDoListRoute> {
                ToDoListScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<PaginationRoute> {
                PaginationScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<InfinitePaginationRoute> {
                InfinitePaginationScreen(
                    onBack = navController::popBackStack,
                )
            }
            composable<PrefetchingRoute> {
                PrefetchingScreen(
                    onBack = navController::popBackStack,
                    toNext = { navController.navigate(PrefetchingNextRoute) },
                    scope = scope,
                )
            }
            composable<PrefetchingNextRoute> {
                PrefetchingNextScreen(
                    onBack = navController::popBackStack,
                    scope = scope,
                )
            }
        }
    }
}
