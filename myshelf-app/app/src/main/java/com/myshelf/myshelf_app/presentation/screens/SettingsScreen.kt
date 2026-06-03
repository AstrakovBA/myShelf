package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    PlaceholderScreen(
        title = "Настройки",
        subtitle = "Тема, язык и профиль (скоро)",
        modifier = modifier
    )
}
