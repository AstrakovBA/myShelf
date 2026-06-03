package com.myshelf.myshelf_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myshelf.myshelf_app.presentation.auth.AuthState
import com.myshelf.myshelf_app.presentation.main.MainScreen
import com.myshelf.myshelf_app.presentation.screens.LoginScreen
import com.myshelf.myshelf_app.presentation.screens.PlaceholderScreen
import com.myshelf.myshelf_app.presentation.screens.RegisterScreen
import com.myshelf.myshelf_app.presentation.viewmodel.AuthViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.ViewModelFactory

@Composable
fun AppNavigation(
    viewModelFactory: ViewModelFactory,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Home.route
        is AuthState.Loading,
        is AuthState.Unauthenticated,
        is AuthState.Error -> Screen.Login.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }

            is AuthState.Unauthenticated -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }

            is AuthState.Loading,
            is AuthState.Error -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            MainScreen(
                viewModelFactory = viewModelFactory,
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                },
                onNavigateToCreateItem = {
                    navController.navigate(Screen.CreateItem.route)
                },
                onNavigateToOutfitConstructor = {
                    navController.navigate(Screen.OutfitConstructor.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(
            route = Screen.ItemDetails.route,
            arguments = listOf(
                navArgument(Screen.ItemDetails.ARG_ITEM_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString(Screen.ItemDetails.ARG_ITEM_ID).orEmpty()
            PlaceholderScreen(
                title = "Детали вещи",
                subtitle = "ID: $itemId"
            )
        }

        composable(Screen.CreateItem.route) {
            PlaceholderScreen(
                title = "Добавить вещь",
                subtitle = "Форма создания (скоро)"
            )
        }

        composable(Screen.OutfitConstructor.route) {
            PlaceholderScreen(
                title = "Конструктор образа",
                subtitle = "Сборка образа (скоро)"
            )
        }

        composable(Screen.Profile.route) {
            PlaceholderScreen(
                title = "Профиль",
                subtitle = "Данные пользователя (скоро)"
            )
        }
    }
}
