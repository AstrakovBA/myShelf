package com.myshelf.myshelf_app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.myshelf.myshelf_app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

class SettingsRepository(
    private val context: Context
) {

    private val dataStore = context.applicationContext.settingsDataStore

    suspend fun saveTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = isDark
        }
    }

    fun getThemeFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_DARK_THEME] ?: false
        }
    }

    suspend fun saveLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    fun getLanguageFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[KEY_LANGUAGE] ?: Constants.DEFAULT_LANGUAGE
        }
    }

    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey(Constants.DS_THEME_MODE)
        private val KEY_LANGUAGE = stringPreferencesKey(Constants.DS_LANGUAGE)
    }
}
