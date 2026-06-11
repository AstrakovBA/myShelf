package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal
import com.myshelf.myshelf_app.data.mapper.OutfitMapper
import com.myshelf.myshelf_app.domain.model.Category
import com.myshelf.myshelf_app.domain.model.Season
import com.myshelf.myshelf_app.presentation.components.OutfitSlotCard
import com.myshelf.myshelf_app.presentation.components.SelectItemDialog
import com.myshelf.myshelf_app.presentation.outfit.OutfitFormState
import com.myshelf.myshelf_app.presentation.outfit.OutfitUpdates
import com.myshelf.myshelf_app.presentation.outfit.SlotType
import com.myshelf.myshelf_app.presentation.viewmodel.ItemsViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.OutfitsViewModel
import com.myshelf.myshelf_app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitConstructorScreen(
    outfitsViewModel: OutfitsViewModel,
    itemsViewModel: ItemsViewModel,
    outfitId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = outfitId != null
    val isSaving by outfitsViewModel.isSaving.collectAsStateWithLifecycle()
    val outfitSaved by outfitsViewModel.outfitSaved.collectAsStateWithLifecycle()
    val errorMessage by outfitsViewModel.errorMessage.collectAsStateWithLifecycle()
    val itemsState by itemsViewModel.items.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var formState by remember(outfitId) { mutableStateOf(OutfitFormState()) }
    var isFormLoading by remember(outfitId) { mutableStateOf(isEditMode) }
    var seasonMenuExpanded by remember { mutableStateOf(false) }
    var selectingSlot by remember { mutableStateOf<SlotType?>(null) }

    val nameRequiredError = stringResource(R.string.error_outfit_name_required)
    val slotsRequiredError = stringResource(R.string.error_outfit_slot_required)

    val allItems: List<ItemLocal> = remember(itemsState) {
        (itemsState as? Resource.Success)?.data.orEmpty()
    }

    val itemsById: Map<String, ItemLocal> = remember(allItems) {
        allItems.associateBy { it.id }
    }

    LaunchedEffect(Unit) {
        itemsViewModel.loadItems()
    }

    LaunchedEffect(outfitId) {
        if (outfitId != null) {
            isFormLoading = true
            outfitsViewModel.loadOutfit(outfitId).collect { outfitWithSlots ->
                if (outfitWithSlots != null) {
                    val outfit = outfitWithSlots.outfit
                    val slots = OutfitFormState.defaultEmptySlots().toMutableMap()
                    outfitWithSlots.slots.forEach { slot ->
                        Category.fromString(slot.slotType)?.let { category ->
                            slots[category] = slot.itemId
                        }
                    }
                    formState = OutfitFormState(
                        name = outfit.name,
                        description = outfit.description.orEmpty(),
                        season = Season.fromString(outfit.season),
                        slots = slots
                    )
                }
                isFormLoading = false
            }
        } else {
            formState = OutfitFormState()
            isFormLoading = false
        }
    }

    LaunchedEffect(outfitSaved) {
        if (outfitSaved) {
            outfitsViewModel.consumeOutfitSaved()
            onNavigateBack()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            outfitsViewModel.clearError()
        }
    }

    selectingSlot?.let { slotType ->
        SelectItemDialog(
            slotType = slotType,
            items = allItems,
            onItemSelected = { item ->
                formState = formState.copy(
                    slots = formState.slots + (slotType to item.id)
                )
                selectingSlot = null
            },
            onDismiss = { selectingSlot = null }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditMode) R.string.edit_outfit else R.string.create_outfit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isSaving) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = {
                            submitOutfit(
                                outfitId, formState, outfitsViewModel,
                                nameRequiredError, slotsRequiredError
                            ) { formState = it }
                        }) {
                            Text(
                                text = stringResource(R.string.save),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isFormLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { formState = formState.copy(name = it, nameError = null) },
                    label = { Text(stringResource(R.string.outfit_name_label)) },
                    isError = formState.nameError != null,
                    supportingText = formState.nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { formState = formState.copy(description = it) },
                    label = { Text(stringResource(R.string.outfit_description_label)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = seasonMenuExpanded,
                    onExpandedChange = { seasonMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.season?.localizedName().orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.item_season_label)) },
                        placeholder = { Text(stringResource(R.string.item_season_optional)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonMenuExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = seasonMenuExpanded,
                        onDismissRequest = { seasonMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.item_season_none)) },
                            onClick = {
                                formState = formState.copy(season = null)
                                seasonMenuExpanded = false
                            }
                        )
                        Season.entries.forEach { season ->
                            DropdownMenuItem(
                                text = { Text(season.localizedName()) },
                                onClick = {
                                    formState = formState.copy(season = season)
                                    seasonMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (formState.slotsError != null) {
                    Text(
                        text = formState.slotsError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = stringResource(R.string.outfit_slots_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(Category.entries, key = { it.name }) { slotType ->
                    val itemId = formState.slots[slotType]
                    OutfitSlotCard(
                        slotType = slotType,
                        selectedItem = itemId?.let { itemsById[it] },
                        onSelectClick = { selectingSlot = slotType },
                        onClearClick = {
                            formState = formState.copy(
                                slots = formState.slots + (slotType to null)
                            )
                        }
                    )
                }
            }

            Button(
                onClick = {
                    submitOutfit(
                        outfitId, formState, outfitsViewModel,
                        nameRequiredError, slotsRequiredError
                    ) { formState = it }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.save_outfit))
            }
        }
        }
    }
}

private fun submitOutfit(
    outfitId: String?,
    formState: OutfitFormState,
    viewModel: OutfitsViewModel,
    nameRequired: String,
    slotsRequired: String,
    onFormStateUpdate: (OutfitFormState) -> Unit
) {
    val validated = formState.validate(nameRequired, slotsRequired)
    onFormStateUpdate(validated)
    if (!validated.isValid) return

    val name = validated.name.trim()
    val description = validated.description.trim().takeIf { it.isNotEmpty() }
    val season = validated.season

    if (outfitId == null) {
        val newOutfitId = OutfitMapper.generateLocalId()
        val slots = validated.slots
            .filter { (_, itemId) -> !itemId.isNullOrBlank() }
            .map { (slotType, itemId) ->
                OutfitSlotLocal(
                    id = OutfitMapper.generateSlotId(),
                    outfitId = newOutfitId,
                    itemId = itemId,
                    slotType = slotType.name
                )
            }

        viewModel.createOutfit(
            name = name,
            description = description,
            season = season?.name,
            slots = slots,
            outfitId = newOutfitId
        )
    } else {
        viewModel.updateOutfit(
            outfitId = outfitId,
            updates = OutfitUpdates(
                name = name,
                description = description,
                season = season,
                slots = validated.slots
            )
        )
    }
}
