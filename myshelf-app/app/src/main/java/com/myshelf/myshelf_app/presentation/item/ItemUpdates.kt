package com.myshelf.myshelf_app.presentation.item

import com.myshelf.myshelf_app.domain.model.Category
import com.myshelf.myshelf_app.domain.model.Season

data class ItemUpdates(
    val name: String,
    val description: String?,
    val category: Category,
    val season: Season?,
    val imageUrl: String?
)
