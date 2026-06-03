package com.myshelf.myshelf_app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.domain.model.Category

@Composable
fun SelectItemDialog(
    slotType: Category,
    items: List<ItemLocal>,
    onItemSelected: (ItemLocal) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredItems = items.filter { item ->
        item.category.equals(slotType.name, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_item_dialog_title, slotType.displayName)
            )
        },
        text = {
            if (filteredItems.isEmpty()) {
                Text(
                    text = stringResource(R.string.select_item_dialog_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(item)
                                    onDismiss()
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            ItemCard(
                                item = item,
                                onClick = {
                                    onItemSelected(item)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
