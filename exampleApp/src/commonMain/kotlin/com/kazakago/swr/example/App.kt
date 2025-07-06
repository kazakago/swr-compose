package com.kazakago.swr.example

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
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
import com.kazakago.swr.example.basic.InfinitePaginationRoute
import com.kazakago.swr.example.basic.InfinitePaginationScreen
import com.kazakago.swr.example.basic.PaginationRoute
import com.kazakago.swr.example.basic.PaginationScreen
import com.kazakago.swr.example.todolist.ToDoListRoute
import com.kazakago.swr.example.todolist.ToDoListScreen
import com.kazakago.swr.example.todolist.server.LocalMockServer
import com.kazakago.swr.example.todolist.server.MockServer
import com.kazakago.swr.example.todolist.server.MockServerSucceed

@Composable
fun App(
    navController: NavHostController = rememberNavController(),
) {
    MaterialTheme {
        val mockServer = remember { mutableStateOf<MockServer>(MockServerSucceed) }
        val isClearCache = remember { mutableStateOf(true) }
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
                        moveToGlobalConfiguration = {},
                        moveToErrorHandling = { navController.navigate(ErrorHandlingRoute) },
                        moveToAutoRevalidation = { navController.navigate(AutoRevalidationRoute) },
                        moveToConditionalFetching = { navController.navigate(ConditionalFetchingRoute) },
                        moveToArguments = { navController.navigate(ArgumentsRoute) },
                        moveToMutation = {},
                        moveToPagination = { navController.navigate(PaginationRoute) },
                        moveToInfinitePagination = { navController.navigate(InfinitePaginationRoute) },
                        moveToPrefetching = {},
                        moveToTodoList = { navController.navigate(ToDoListRoute) },
                    )
                }
                composable<DataFetchingRoute> {
                    DataFetchingScreen(
                        onBack = navController::popBackStack,
                    )
                }
//                composable("global_configuration") {
//                    GlobalConfigurationScreen(navController)
//                }
                composable<ErrorHandlingRoute> {
                    ErrorHandlingScreen(navController)
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
//                composable("mutation") {
//                    MutationScreen(navController)
//                }
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
//                composable("prefetching") {
//                    PrefetchingScreen(navController, scope)
//                }
//                composable("prefetching_next") {
//                    PrefetchingNextScreen(navController, scope)
//                }
            }
        }
    }
}
