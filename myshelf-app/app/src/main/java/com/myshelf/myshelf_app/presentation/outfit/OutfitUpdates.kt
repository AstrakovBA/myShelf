package com.myshelf.myshelf_app.presentation.outfit

import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal

data class OutfitUpdates(
    val name: String,
    val description: String? = null,
    val season: String? = null,
    val slots: List<OutfitSlotLocal>
)
