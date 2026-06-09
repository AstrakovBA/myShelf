package com.myshelf.myshelf_app.presentation.item

import com.myshelf.myshelf_app.domain.model.Category
import com.myshelf.myshelf_app.domain.model.Season

data class ItemFormState(
    val name: String = "",
    val description: String = "",
    val category: Category? = null,
    val season: Season? = null,
    val imageUrl: String = "",
    val nameError: String? = null,
    val categoryError: String? = null
) {
    fun validate(nameRequired: String, categoryRequired: String): ItemFormState {
        val trimmedName = name.trim()
        return copy(
            nameError = if (trimmedName.isEmpty()) nameRequired else null,
            categoryError = if (category == null) categoryRequired else null
        )
    }

    val isValid: Boolean
        get() = name.trim().isNotEmpty() && category != null

    val hasErrors: Boolean
        get() = nameError != null || categoryError != null
}
