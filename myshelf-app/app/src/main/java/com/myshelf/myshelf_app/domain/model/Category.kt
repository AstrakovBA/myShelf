package com.myshelf.myshelf_app.domain.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.myshelf.myshelf_app.R

enum class Category(@StringRes val labelRes: Int) {
    HEADWEAR(R.string.category_headwear),
    TOP(R.string.category_top),
    BOTTOM(R.string.category_bottom),
    SHOES(R.string.category_shoes),
    OUTERWEAR(R.string.category_outerwear),
    ACCESSORIES(R.string.category_accessories);

    @Composable
    fun localizedName(): String = stringResource(labelRes)

    fun getLocalizedName(context: Context): String = context.getString(labelRes)

    companion object {
        fun fromString(value: String?): Category? {
            if (value.isNullOrBlank()) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

        fun displayNameFor(value: String?, context: Context): String {
            return fromString(value)?.getLocalizedName(context) ?: value.orEmpty()
        }
    }
}
