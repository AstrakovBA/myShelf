package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.domain.model.Category
import com.myshelf.myshelf_app.domain.model.Season
import com.myshelf.myshelf_app.presentation.item.ItemFormState
import com.myshelf.myshelf_app.presentation.item.ItemUpdates
import com.myshelf.myshelf_app.presentation.viewmodel.ItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    viewModel: ItemsViewModel,
    itemId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = itemId != null
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val itemSaved by viewModel.itemSaved.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var formState by remember(itemId) { mutableStateOf(ItemFormState()) }
    var isFormLoading by remember(itemId) { mutableStateOf(isEditMode) }

    val nameRequiredError = stringResource(R.string.error_item_name_required)
    val categoryRequiredError = stringResource(R.string.error_category_required)

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var seasonMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        if (itemId != null) {
            isFormLoading = true
            viewModel.loadItem(itemId).collect { item ->
                if (item != null) {
                    formState = item.toFormState()
                }
                isFormLoading = false
            }
        } else {
            formState = ItemFormState()
            isFormLoading = false
        }
    }

    LaunchedEffect(itemSaved) {
        if (itemSaved) {
            viewModel.consumeItemSaved()
            onNavigateBack()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditMode) R.string.edit_item_title else R.string.create_item_title
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
                            submitForm(
                                itemId, formState, viewModel,
                                nameRequiredError, categoryRequiredError
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
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImagePickerSection(
                imageUrl = formState.imageUrl,
                onAddPhotoClick = {
                    formState = formState.copy(
                        imageUrl = formState.imageUrl.ifBlank {
                            "https://via.placeholder.com/300x400?text=MyShelf"
                        }
                    )
                }
            )

            OutlinedTextField(
                value = formState.name,
                onValueChange = { formState = formState.copy(name = it, nameError = null) },
                label = { Text(stringResource(R.string.item_name_label)) },
                isError = formState.nameError != null,
                supportingText = formState.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.description,
                onValueChange = { formState = formState.copy(description = it) },
                label = { Text(stringResource(R.string.item_description_label)) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = formState.category?.localizedName().orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.item_category_label)) },
                    isError = formState.categoryError != null,
                    supportingText = formState.categoryError?.let { { Text(it) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    Category.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.localizedName()) },
                            onClick = {
                                formState = formState.copy(category = category, categoryError = null)
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonMenuExpanded) },
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

            OutlinedTextField(
                value = formState.imageUrl,
                onValueChange = { formState = formState.copy(imageUrl = it) },
                label = { Text(stringResource(R.string.item_image_url_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    submitForm(
                        itemId, formState, viewModel,
                        nameRequiredError, categoryRequiredError
                    ) { formState = it }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_item))
            }
        }
        }
    }
}

private fun ItemLocal.toFormState(): ItemFormState {
    return ItemFormState(
        name = name,
        description = description.orEmpty(),
        category = Category.fromString(category),
        season = Season.fromString(season),
        imageUrl = imageUrl.orEmpty()
    )
}

private fun submitForm(
    itemId: String?,
    formState: ItemFormState,
    viewModel: ItemsViewModel,
    nameRequired: String,
    categoryRequired: String,
    onFormStateUpdate: (ItemFormState) -> Unit
) {
    val validated = formState.validate(nameRequired, categoryRequired)
    onFormStateUpdate(validated)
    if (!validated.isValid) return

    val name = validated.name.trim()
    val description = validated.description.trim().takeIf { it.isNotEmpty() }
    val category = validated.category!!
    val season = validated.season
    val imageUrl = validated.imageUrl.trim().takeIf { it.isNotEmpty() }

    if (itemId == null) {
        viewModel.createItem(
            name = name,
            description = description,
            category = category.name,
            season = season?.name,
            imageUrl = imageUrl
        )
    } else {
        viewModel.updateItem(
            itemId = itemId,
            updates = ItemUpdates(
                name = name,
                description = description,
                category = category,
                season = season,
                imageUrl = imageUrl
            )
        )
    }
}

@Composable
private fun ImagePickerSection(
    imageUrl: String,
    onAddPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.item_image_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedButton(onClick = onAddPhotoClick) {
            Icon(Icons.Default.AddAPhoto, contentDescription = null)
            Text(
                text = stringResource(R.string.add_photo),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
