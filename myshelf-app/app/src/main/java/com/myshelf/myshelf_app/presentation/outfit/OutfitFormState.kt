package com.myshelf.myshelf_app.presentation.outfit

import com.myshelf.myshelf_app.domain.model.Category
import com.myshelf.myshelf_app.domain.model.Season

typealias SlotType = Category

data class OutfitFormState(
    val name: String = "",
    val description: String = "",
    val season: Season? = null,
    val slots: Map<SlotType, String?> = defaultEmptySlots(),
    val nameError: String? = null,
    val slotsError: String? = null
) {
    fun validate(): OutfitFormState {
        val hasFilledSlot = slots.values.any { !it.isNullOrBlank() }
        return copy(
            nameError = if (name.trim().isEmpty()) "Укажите название образа" else null,
            slotsError = if (!hasFilledSlot) "Выберите хотя бы одну вещь для образа" else null
        )
    }

    val isValid: Boolean
        get() = name.trim().isNotEmpty() && slots.values.any { !it.isNullOrBlank() }

    companion object {
        fun defaultEmptySlots(): Map<SlotType, String?> =
            Category.entries.associateWith { null }
    }
}
