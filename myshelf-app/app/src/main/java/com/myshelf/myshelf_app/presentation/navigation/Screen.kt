package com.myshelf.myshelf_app.presentation.navigation

sealed class Screen(val route: String) {

    data object Login : Screen("login")

    data object Register : Screen("register")

    data object Home : Screen("home")

    data object ItemsList : Screen("items_list")

    data object ItemDetails : Screen("item_details/{itemId}") {
        const val ARG_ITEM_ID = "itemId"

        fun createRoute(itemId: String): String = "item_details/$itemId"
    }

    data object CreateItem : Screen("create_item")

    data object OutfitsList : Screen("outfits_list")

    data object OutfitConstructor : Screen("outfit_constructor")

    data object Settings : Screen("settings")

    data object Profile : Screen("profile")
}
