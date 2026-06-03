package com.myshelf.myshelf_app.domain.model

enum class Category(val displayName: String) {
    HEADWEAR("Головной убор"),
    TOP("Верх"),
    BOTTOM("Низ"),
    SHOES("Обувь"),
    OUTERWEAR("Верхняя одежда"),
    ACCESSORIES("Аксессуары");

    companion object {
        fun fromString(value: String?): Category? {
            if (value.isNullOrBlank()) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

        fun displayNameFor(value: String?): String {
            return fromString(value)?.displayName ?: value.orEmpty()
        }
    }
}
