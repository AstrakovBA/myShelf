package com.myshelf.myshelf_app.presentation.navigation

/**
 * Маршруты навигации приложения.
 */
object NavRoutes {

    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val WARDROBE = "wardrobe"
    const val ITEM_DETAIL = "item_detail/{itemId}"
    const val ADD_ITEM = "add_item"
    const val OUTFITS = "outfits"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    fun itemDetail(itemId: Long): String = "item_detail/$itemId"
}
