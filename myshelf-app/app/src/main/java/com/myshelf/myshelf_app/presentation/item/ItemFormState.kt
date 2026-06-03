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
    fun validate(): ItemFormState {
        val trimmedName = name.trim()
        return copy(
            nameError = when {
                trimmedName.isEmpty() -> "Укажите название вещи"
                else -> null
            },
            categoryError = when {
                category == null -> "Выберите категорию"
                else -> null
            }
        )
    }

    val isValid: Boolean
        get() = name.trim().isNotEmpty() && category != null

    val hasErrors: Boolean
        get() = nameError != null || categoryError != null
}
