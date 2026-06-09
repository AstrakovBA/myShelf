package com.myshelf.myshelf_app.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.data.repository.AuthRepository
import com.myshelf.myshelf_app.data.repository.ItemsRepository
import com.myshelf.myshelf_app.data.repository.OutfitsRepository
import com.myshelf.myshelf_app.data.repository.SettingsRepository
import com.myshelf.myshelf_app.presentation.BaseViewModel
import com.myshelf.myshelf_app.presentation.settings.Theme
import com.myshelf.myshelf_app.util.Constants
import com.myshelf.myshelf_app.util.LocaleManager
import com.myshelf.myshelf_app.util.Result
import com.myshelf.myshelf_app.util.StringResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val itemsRepository: ItemsRepository,
    private val outfitsRepository: OutfitsRepository,
    private val authRepository: AuthRepository,
    private val userId: String
) : BaseViewModel() {

    private val _theme = MutableStateFlow<Theme>(Theme.System)
    val theme: StateFlow<Theme> = _theme.asStateFlow()

    private val _language = MutableStateFlow(Constants.DEFAULT_LANGUAGE)
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isClearingCache = MutableStateFlow(false)
    val isClearingCache: StateFlow<Boolean> = _isClearingCache.asStateFlow()

    private val _isChangingPassword = MutableStateFlow(false)
    val isChangingPassword: StateFlow<Boolean> = _isChangingPassword.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.getThemeModeFlow().collect { mode ->
                    _theme.value = mode.toTheme()
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }

        viewModelScope.launch {
            try {
                settingsRepository.getLanguageFlow().collect { lang ->
                    _language.value = lang
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                settingsRepository.saveThemeMode(theme.toStorageValue())
                _theme.value = theme
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                settingsRepository.saveLanguage(language)
                _language.value = language
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun applyLanguage(language: String, activity: Activity) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                settingsRepository.saveLanguage(language)
                _language.value = language
                LocaleManager.applyToActivity(activity, language)
                StringResources.init(
                    LocaleManager.wrapContext(activity.applicationContext, language)
                )
                activity.recreate()
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun syncAll() {
        if (authRepository.isGuestMode()) {
            _errorMessage.value = StringResources.getString(R.string.error_sync_guest_mode)
            return
        }
        if (userId.isBlank()) {
            _errorMessage.value = StringResources.getString(R.string.error_auth_required)
            return
        }

        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _errorMessage.value = null
                _successMessage.value = null

                val itemsResult = itemsRepository.syncItemsWithServer(userId)
                val outfitsResult = outfitsRepository.syncOutfitsWithServer(userId)

                when {
                    itemsResult is Result.Error -> _errorMessage.value = itemsResult.message
                    outfitsResult is Result.Error -> _errorMessage.value = outfitsResult.message
                    else -> _successMessage.value = StringResources.getString(R.string.success_sync_complete)
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearCache() {
        if (userId.isBlank()) {
            _errorMessage.value = StringResources.getString(R.string.error_auth_required)
            return
        }

        viewModelScope.launch {
            try {
                _isClearingCache.value = true
                _errorMessage.value = null
                _successMessage.value = null

                itemsRepository.clearLocalCache(userId)
                outfitsRepository.clearLocalCache(userId)

                _successMessage.value = StringResources.getString(R.string.success_cache_cleared)
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isClearingCache.value = false
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        when {
            oldPassword.isBlank() -> {
                _errorMessage.value = StringResources.getString(R.string.error_old_password_required)
                return
            }

            newPassword.length < 6 -> {
                _errorMessage.value = StringResources.getString(R.string.error_new_password_too_short)
                return
            }

            newPassword != confirmPassword -> {
                _errorMessage.value = StringResources.getString(R.string.error_passwords_mismatch)
                return
            }
        }

        viewModelScope.launch {
            try {
                _isChangingPassword.value = true
                _errorMessage.value = null
                _successMessage.value = null

                when (val result = authRepository.changePassword(oldPassword, newPassword)) {
                    is Result.Success -> _successMessage.value =
                        StringResources.getString(R.string.success_password_changed)
                    is Result.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isChangingPassword.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
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
