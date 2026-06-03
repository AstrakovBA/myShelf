package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.presentation.viewmodel.OutfitsViewModel

@Composable
fun OutfitsListScreen(
    viewModel: OutfitsViewModel,
    onCreateOutfitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.loadOutfits()
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateOutfitClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_outfit)
                )
            }
        }
    ) { innerPadding ->
        PlaceholderScreen(
            title = "Мои образы",
            subtitle = "Список образов (скоро)",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
