package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ItemsListScreen(
    onItemClick: (String) -> Unit = {},
    onCreateItemClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    PlaceholderScreen(
        title = "Мои вещи",
        subtitle = "Список вещей гардероба (скоро)",
        modifier = modifier
    )
}
