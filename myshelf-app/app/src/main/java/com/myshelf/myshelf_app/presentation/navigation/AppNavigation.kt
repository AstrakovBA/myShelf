package com.myshelf.myshelf_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.myshelf.myshelf_app.presentation.screens.CreateItemScreen
import com.myshelf.myshelf_app.presentation.screens.ItemDetailsScreen
import com.myshelf.myshelf_app.presentation.screens.LoginScreen
import com.myshelf.myshelf_app.presentation.screens.OutfitConstructorScreen
import com.myshelf.myshelf_app.presentation.screens.PlaceholderScreen
import com.myshelf.myshelf_app.presentation.screens.RegisterScreen
import com.myshelf.myshelf_app.presentation.viewmodel.AuthViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.ItemsViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.OutfitsViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.SettingsViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.ViewModelFactory

@Composable
fun AppNavigation(
    viewModelFactory: ViewModelFactory,
    settingsViewModel: SettingsViewModel,
    appVersion: String,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val startDestination = when (authState) {
        is AuthState.Authenticated,
        is AuthState.Guest -> Screen.Home.route
        is AuthState.Loading,
        is AuthState.Unauthenticated,
        is AuthState.Error -> Screen.Login.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated,
            is AuthState.Guest -> {
                val currentRoute = navController.currentDestination?.route
                if (!isAuthenticatedDestination(currentRoute)) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
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

        composable(Screen.Home.route) { entry ->
            val itemsViewModel: ItemsViewModel = viewModel(entry, factory = viewModelFactory)
            val outfitsViewModel: OutfitsViewModel = viewModel(entry, factory = viewModelFactory)
            val isGuestMode = authState is AuthState.Guest
            MainScreen(
                itemsViewModel = itemsViewModel,
                outfitsViewModel = outfitsViewModel,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel,
                isGuestMode = isGuestMode,
                appVersion = appVersion,
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                },
                onNavigateToCreateItem = {
                    navController.navigate(Screen.CreateItem.createRoute())
                },
                onNavigateToEditItem = { itemId ->
                    navController.navigate(Screen.CreateItem.createRoute(itemId))
                },
                onNavigateToOutfitConstructor = {
                    navController.navigate(Screen.OutfitConstructor.createRoute())
                },
                onNavigateToEditOutfit = { outfitId ->
                    navController.navigate(Screen.OutfitConstructor.createRoute(outfitId))
                },
                onNavigateToProfile = {
                    if (!authViewModel.isGuestMode()) {
                        navController.navigate(Screen.Profile.route)
                    }
                },
                onLogout = {
                    authViewModel.logoutCurrentSession()
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
            val itemsViewModel: ItemsViewModel = viewModel(
                remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Home.route)
                },
                factory = viewModelFactory
            )
            ItemDetailsScreen(
                itemId = itemId,
                viewModel = itemsViewModel,
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.CreateItem.createRoute(id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateItem.route) { backStackEntry ->
            val itemsViewModel: ItemsViewModel = viewModel(
                remember(backStackEntry) { navController.getBackStackEntry(Screen.Home.route) },
                factory = viewModelFactory
            )
            CreateItemScreen(
                viewModel = itemsViewModel,
                itemId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateItem.ROUTE_WITH_ID,
            arguments = listOf(
                navArgument(Screen.CreateItem.ARG_ITEM_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString(Screen.CreateItem.ARG_ITEM_ID)
            val itemsViewModel: ItemsViewModel = viewModel(
                remember(backStackEntry) { navController.getBackStackEntry(Screen.Home.route) },
                factory = viewModelFactory
            )
            CreateItemScreen(
                viewModel = itemsViewModel,
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OutfitConstructor.route) { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val outfitsViewModel: OutfitsViewModel = viewModel(homeBackStackEntry, factory = viewModelFactory)
            val itemsViewModel: ItemsViewModel = viewModel(homeBackStackEntry, factory = viewModelFactory)
            OutfitConstructorScreen(
                outfitsViewModel = outfitsViewModel,
                itemsViewModel = itemsViewModel,
                outfitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.OutfitConstructor.ROUTE_WITH_ID,
            arguments = listOf(
                navArgument(Screen.OutfitConstructor.ARG_OUTFIT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getString(Screen.OutfitConstructor.ARG_OUTFIT_ID)
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val outfitsViewModel: OutfitsViewModel = viewModel(homeBackStackEntry, factory = viewModelFactory)
            val itemsViewModel: ItemsViewModel = viewModel(homeBackStackEntry, factory = viewModelFactory)
            OutfitConstructorScreen(
                outfitsViewModel = outfitsViewModel,
                itemsViewModel = itemsViewModel,
                outfitId = outfitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            PlaceholderScreen(
                titleRes = com.myshelf.myshelf_app.R.string.profile_title,
                subtitleRes = com.myshelf.myshelf_app.R.string.profile_subtitle
            )
        }
    }
}

private fun isAuthenticatedDestination(route: String?): Boolean {
    if (route == null) return false
    return route == Screen.Home.route ||
        route.startsWith("item_details/") ||
        route == Screen.CreateItem.route ||
        route.startsWith("${Screen.CreateItem.route}/") ||
        route == Screen.OutfitConstructor.route ||
        route.startsWith("${Screen.OutfitConstructor.route}/") ||
        route == Screen.Profile.route
}
