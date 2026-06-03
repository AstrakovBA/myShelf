package com.myshelf.myshelf_app.domain.model

enum class Season(val displayName: String) {
    WINTER("Зима"),
    SPRING("Весна"),
    SUMMER("Лето"),
    AUTUMN("Осень"),
    ALL_SEASONS("Всесезонное");

    companion object {
        fun fromString(value: String?): Season? {
            if (value.isNullOrBlank()) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

        fun displayNameFor(value: String?): String? {
            val season = fromString(value) ?: return null
            return season.displayName
        }
    }
}
