package com.myshelf.myshelf_app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.myshelf.myshelf_app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

class SettingsRepository(
    private val context: Context
) {

    private val dataStore = context.applicationContext.settingsDataStore

    suspend fun saveTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = if (isDark) THEME_DARK else THEME_LIGHT
        }
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    fun getThemeFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_THEME_MODE] == THEME_DARK
        }
    }

    fun getThemeModeFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[KEY_THEME_MODE] ?: THEME_SYSTEM
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

    suspend fun getLanguage(): String {
        return dataStore.data.first()[KEY_LANGUAGE] ?: Constants.DEFAULT_LANGUAGE
    }

    fun getLanguageBlocking(): String {
        return runBlocking { getLanguage() }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey(Constants.DS_THEME_MODE)
        private val KEY_LANGUAGE = stringPreferencesKey(Constants.DS_LANGUAGE)

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }
}
