package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.local.relation.OutfitWithSlots
import com.myshelf.myshelf_app.presentation.components.GuestModeTopBarTitle
import com.myshelf.myshelf_app.presentation.components.OutfitCard
import com.myshelf.myshelf_app.presentation.components.guestModeTopAppBarColors
import com.myshelf.myshelf_app.presentation.components.OutfitDetailsDialog
import com.myshelf.myshelf_app.presentation.components.buildOutfitSlotDisplays
import com.myshelf.myshelf_app.presentation.components.resolveOutfitSlotItems
import com.myshelf.myshelf_app.presentation.viewmodel.ItemsViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.OutfitsViewModel
import com.myshelf.myshelf_app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitsListScreen(
    viewModel: OutfitsViewModel,
    itemsViewModel: ItemsViewModel,
    isGuestMode: Boolean = false,
    onCreateOutfitClick: () -> Unit = {},
    onEditOutfitClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val outfitsState by viewModel.outfits.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val itemsState by itemsViewModel.items.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedOutfit by remember { mutableStateOf<OutfitWithSlots?>(null) }
    var outfitToDelete by remember { mutableStateOf<OutfitWithSlots?>(null) }

    val itemsById: Map<String, ItemLocal> = remember(itemsState) {
        (itemsState as? Resource.Success)?.data.orEmpty().associateBy { it.id }
    }

    LaunchedEffect(Unit) {
        viewModel.loadOutfits()
        itemsViewModel.loadItems()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    selectedOutfit?.let { outfit ->
        OutfitDetailsDialog(
            outfitWithSlots = outfit,
            slotDisplays = buildOutfitSlotDisplays(outfit, itemsById),
            onEditClick = {
                selectedOutfit = null
                onEditOutfitClick(outfit.outfit.id)
            },
            onDeleteClick = {
                selectedOutfit = null
                outfitToDelete = outfit
            },
            onDismiss = { selectedOutfit = null }
        )
    }

    outfitToDelete?.let { outfit ->
        AlertDialog(
            onDismissRequest = { outfitToDelete = null },
            title = { Text(stringResource(R.string.delete_outfit)) },
            text = {
                Text(stringResource(R.string.delete_outfit_confirmation, outfit.outfit.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteOutfit(outfit.outfit.id)
                        outfitToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete_outfit),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { outfitToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    GuestModeTopBarTitle(
                        title = stringResource(R.string.nav_outfits),
                        isGuestMode = isGuestMode
                    )
                },
                colors = guestModeTopAppBarColors(isGuestMode)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateOutfitClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_outfit)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = outfitsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                is Resource.Success -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.syncOutfits() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.data.isEmpty()) {
                            OutfitsEmptyState(modifier = Modifier.fillMaxSize())
                        } else {
                            OutfitsGridContent(
                                outfits = state.data,
                                itemsById = itemsById,
                                onOutfitClick = { selectedOutfit = it },
                                onEditOutfit = onEditOutfitClick,
                                onDeleteOutfit = { viewModel.deleteOutfit(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutfitsGridContent(
    outfits: List<OutfitWithSlots>,
    itemsById: Map<String, ItemLocal>,
    onOutfitClick: (OutfitWithSlots) -> Unit,
    onEditOutfit: (String) -> Unit,
    onDeleteOutfit: (String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        items(outfits, key = { it.outfit.id }) { outfitWithSlots ->
            val outfitId = outfitWithSlots.outfit.id
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteOutfit(outfitId)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_outfit),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            ) {
                OutfitCard(
                    outfitWithSlots = outfitWithSlots,
                    slotItems = resolveOutfitSlotItems(outfitWithSlots, itemsById),
                    onClick = { onOutfitClick(outfitWithSlots) },
                    onEditClick = { onEditOutfit(outfitId) },
                    onDeleteClick = { onDeleteOutfit(outfitId) }
                )
            }
        }
    }
}

@Composable
private fun OutfitsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Style,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.outfits_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.outfits_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
