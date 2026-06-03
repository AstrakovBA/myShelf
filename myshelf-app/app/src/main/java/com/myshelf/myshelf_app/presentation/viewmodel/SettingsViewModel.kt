package com.myshelf.myshelf_app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.data.repository.SettingsRepository
import com.myshelf.myshelf_app.presentation.BaseViewModel
import com.myshelf.myshelf_app.presentation.settings.Theme
import com.myshelf.myshelf_app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : BaseViewModel() {

    private val _theme = MutableStateFlow<Theme>(Theme.System)
    val theme: StateFlow<Theme> = _theme.asStateFlow()

    private val _language = MutableStateFlow(Constants.DEFAULT_LANGUAGE)
    val language: StateFlow<String> = _language.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            try {
                repository.getThemeModeFlow().collect { mode ->
                    _theme.value = mode.toTheme()
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }

        viewModelScope.launch {
            try {
                repository.getLanguageFlow().collect { lang ->
                    _language.value = lang
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val nextTheme = when (_theme.value) {
                    Theme.Light -> Theme.Dark
                    Theme.Dark -> Theme.System
                    Theme.System -> Theme.Light
                }
                repository.saveThemeMode(nextTheme.toStorageValue())
                _theme.value = nextTheme
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.saveLanguage(language)
                _language.value = language
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun String.toTheme(): Theme = when (lowercase()) {
        SettingsRepository.THEME_DARK -> Theme.Dark
        SettingsRepository.THEME_LIGHT -> Theme.Light
        else -> Theme.System
    }

    private fun Theme.toStorageValue(): String = when (this) {
        Theme.Light -> SettingsRepository.THEME_LIGHT
        Theme.Dark -> SettingsRepository.THEME_DARK
        Theme.System -> SettingsRepository.THEME_SYSTEM
    }
}
