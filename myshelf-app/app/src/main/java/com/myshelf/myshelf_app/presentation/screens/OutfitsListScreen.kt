package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OutfitsListScreen(
    onCreateOutfitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    PlaceholderScreen(
        title = "Мои образы",
        subtitle = "Список образов (скоро)",
        modifier = modifier
    )
}
