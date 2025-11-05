package com.kazakago.swr.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun App(
    modifier: Modifier = Modifier,
) = MaterialTheme {
    val scope = rememberCoroutineScope()
    val mockServer = remember { mutableStateOf<MockServer>(MockServerSucceed) }
    val isClearCache = remember { mutableStateOf(true) }
    val config = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(MainRoute::class, MainRoute.serializer())
                subclass(DataFetchingRoute::class, DataFetchingRoute.serializer())
                subclass(GlobalConfigurationRoute::class, GlobalConfigurationRoute.serializer())
                subclass(ErrorHandlingRoute::class, ErrorHandlingRoute.serializer())
                subclass(AutoRevalidationRoute::class, AutoRevalidationRoute.serializer())
                subclass(ConditionalFetchingRoute::class, ConditionalFetchingRoute.serializer())
                subclass(ArgumentsRoute::class, ArgumentsRoute.serializer())
                subclass(MutationRoute::class, MutationRoute.serializer())
                subclass(PaginationRoute::class, PaginationRoute.serializer())
                subclass(InfinitePaginationRoute::class, InfinitePaginationRoute.serializer())
                subclass(PrefetchingRoute::class, PrefetchingRoute.serializer())
                subclass(ToDoListRoute::class, ToDoListRoute.serializer())
            }
        }
    }
    val backStack = rememberNavBackStack(config, MainRoute)
    val entryProvider = entryProvider {
        entry<MainRoute> {
            MainScreen(
                mockServer = mockServer,
                isClearCache = isClearCache,
                moveToDataFetching = { backStack.add(DataFetchingRoute) },
                moveToGlobalConfiguration = { backStack.add(GlobalConfigurationRoute) },
                moveToErrorHandling = { backStack.add(ErrorHandlingRoute) },
                moveToAutoRevalidation = { backStack.add(AutoRevalidationRoute) },
                moveToConditionalFetching = { backStack.add(ConditionalFetchingRoute) },
                moveToArguments = { backStack.add(ArgumentsRoute) },
                moveToMutation = { backStack.add(MutationRoute) },
                moveToPagination = { backStack.add(PaginationRoute) },
                moveToInfinitePagination = { backStack.add(InfinitePaginationRoute) },
                moveToPrefetching = { backStack.add(PrefetchingRoute) },
                moveToTodoList = { backStack.add(ToDoListRoute) },
            )
        }
        entry<DataFetchingRoute> {
            DataFetchingScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<GlobalConfigurationRoute> {
            GlobalConfigurationScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<ErrorHandlingRoute> {
            ErrorHandlingScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<AutoRevalidationRoute> {
            AutoRevalidationScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<ConditionalFetchingRoute> {
            ConditionalFetchingScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<ArgumentsRoute> {
            ArgumentsScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<MutationRoute> {
            MutationScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<ToDoListRoute> {
            ToDoListScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<PaginationRoute> {
            PaginationScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<InfinitePaginationRoute> {
            InfinitePaginationScreen(
                onBack = backStack::removeLastOrNull,
            )
        }
        entry<PrefetchingRoute> {
            PrefetchingScreen(
                onBack = backStack::removeLastOrNull,
                toNext = { backStack.add(PrefetchingNextRoute) },
                scope = scope,
            )
        }
        entry<PrefetchingNextRoute> {
            PrefetchingNextScreen(
                onBack = backStack::removeLastOrNull,
                scope = scope,
            )
        }
    }
    CompositionLocalProvider(LocalMockServer provides mockServer.value) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider,
        )
    }
}
