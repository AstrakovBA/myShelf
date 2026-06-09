package com.myshelf.myshelf_app.domain.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.myshelf.myshelf_app.R

enum class Season(@StringRes val labelRes: Int) {
    WINTER(R.string.season_winter),
    SPRING(R.string.season_spring),
    SUMMER(R.string.season_summer),
    AUTUMN(R.string.season_autumn),
    ALL_SEASONS(R.string.season_all_seasons);

    @Composable
    fun localizedName(): String = stringResource(labelRes)

    fun getLocalizedName(context: Context): String = context.getString(labelRes)

    companion object {
        fun fromString(value: String?): Season? {
            if (value.isNullOrBlank()) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

        fun displayNameFor(value: String?, context: Context): String? {
            return fromString(value)?.getLocalizedName(context)
        }
    }
}
