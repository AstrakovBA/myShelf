package com.myshelf.myshelf_app.presentation.outfit

import com.myshelf.myshelf_app.domain.model.Season

data class OutfitUpdates(
    val name: String,
    val description: String?,
    val season: Season?,
    val slots: Map<SlotType, String?>
)
