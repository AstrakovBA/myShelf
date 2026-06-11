package com.myshelf.myshelf_app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.local.relation.OutfitWithSlots
import com.myshelf.myshelf_app.domain.model.Category

data class OutfitSlotDisplay(
    val slotType: Category,
    val item: ItemLocal?
)

@Composable
fun OutfitDetailsDialog(
    outfitWithSlots: OutfitWithSlots,
    slotDisplays: List<OutfitSlotDisplay>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = outfitWithSlots.outfit.name)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(slotDisplays, key = { it.slotType.name }) { slot ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = slot.slotType.localizedName(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (slot.item != null) {
                            ItemCard(
                                item = slot.item,
                                onClick = {}
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.outfit_slot_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onEditClick) {
                Text(stringResource(R.string.outfit_details_edit))
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onDeleteClick) {
                    Text(
                        text = stringResource(R.string.delete_outfit),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.outfit_details_close))
                }
            }
        }
    )
}

fun buildOutfitSlotDisplays(
    outfitWithSlots: OutfitWithSlots,
    itemsById: Map<String, ItemLocal>
): List<OutfitSlotDisplay> {
    val slotsByType = outfitWithSlots.slots.associateBy { it.slotType.uppercase() }
    return Category.entries.map { category ->
        val slot = slotsByType[category.name]
        OutfitSlotDisplay(
            slotType = category,
            item = slot?.itemId?.let { itemsById[it] }
        )
    }.filter { it.item != null }
}
