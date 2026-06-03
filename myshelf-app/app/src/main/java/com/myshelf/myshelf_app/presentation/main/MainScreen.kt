package com.myshelf.myshelf_app.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.presentation.navigation.Screen
import com.myshelf.myshelf_app.presentation.screens.ItemsListScreen
import com.myshelf.myshelf_app.presentation.screens.OutfitsListScreen
import com.myshelf.myshelf_app.presentation.screens.SettingsScreen
import com.myshelf.myshelf_app.presentation.viewmodel.ItemsViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.ViewModelFactory

private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModelFactory: ViewModelFactory,
    itemsViewModel: ItemsViewModel,
    onNavigateToItemDetails: (String) -> Unit,
    onNavigateToCreateItem: () -> Unit,
    onNavigateToOutfitConstructor: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.ItemsList,
            labelRes = R.string.nav_items,
            icon = { Icon(Icons.Default.Checkroom, contentDescription = null) }
        ),
        BottomNavItem(
            screen = Screen.OutfitsList,
            labelRes = R.string.nav_outfits,
            icon = { Icon(Icons.Default.Style, contentDescription = null) }
        ),
        BottomNavItem(
            screen = Screen.Settings,
            labelRes = R.string.nav_settings,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
    )

    val showMainTopBar = currentRoute != Screen.ItemsList.route

    val topBarTitle = when (currentRoute) {
        Screen.OutfitsList.route -> stringResource(R.string.nav_outfits)
        Screen.Settings.route -> stringResource(R.string.nav_settings)
        else -> stringResource(R.string.app_name)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showMainTopBar) {
                TopAppBar(
                    title = { Text(topBarTitle) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(item.screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = item.icon,
                        label = { Text(stringResource(item.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.ItemsList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ItemsList.route) {
                ItemsListScreen(
                    viewModel = itemsViewModel,
                    onItemClick = onNavigateToItemDetails,
                    onCreateItemClick = onNavigateToCreateItem
                )
            }
            composable(Screen.OutfitsList.route) {
                OutfitsListScreen(
                    onCreateOutfitClick = onNavigateToOutfitConstructor
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onProfileClick = onNavigateToProfile
                )
            }
        }
    }
}
