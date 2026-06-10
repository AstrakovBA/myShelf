package com.myshelf.myshelf_app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.data.local.entity.UserLocal
import com.myshelf.myshelf_app.data.repository.AuthRepository
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.presentation.BaseViewModel
import com.myshelf.myshelf_app.presentation.auth.AuthState
import com.myshelf.myshelf_app.util.Result
import com.myshelf.myshelf_app.util.StringResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : BaseViewModel() {

    private val _currentUser = MutableStateFlow<UserLocal?>(null)
    val currentUser: StateFlow<UserLocal?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    private val _profileSaved = MutableStateFlow(false)
    val profileSaved: StateFlow<Boolean> = _profileSaved.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    private val _accountDeleted = MutableStateFlow(false)
    val accountDeleted: StateFlow<Boolean> = _accountDeleted.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                when {
                    repository.isGuestMode() -> {
                        val guestId = repository.getUserId()
                        if (guestId != null) {
                            _currentUser.value = repository.getCurrentUser()
                            _authState.value = AuthState.Guest(guestId)
                        } else {
                            _currentUser.value = null
                            _authState.value = AuthState.Unauthenticated
                        }
                    }

                    repository.isLoggedIn() -> {
                        val user = repository.getCurrentUser()
                        _currentUser.value = user
                        val userId = repository.getUserId()
                        if (userId != null) {
                            _authState.value = AuthState.Authenticated(userId)
                        } else {
                            _authState.value = AuthState.Unauthenticated
                        }
                    }

                    else -> {
                        _currentUser.value = null
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(getErrorMessage(e))
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                _errorMessage.value = null

                when (val result = repository.register(email, password, displayName)) {
                    is Result.Success -> {
                        val userId = result.data.userId ?: result.data.profile?.id
                        if (userId != null) {
                            _currentUser.value = repository.getCurrentUser()
                            _authState.value = AuthState.Authenticated(userId)
                        } else {
                            _authState.value = AuthState.Error(
                                StringResources.getString(R.string.error_user_profile_not_found)
                            )
                        }
                    }

                    is Result.Error -> {
                        _errorMessage.value = result.message
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                val message = getErrorMessage(e)
                _errorMessage.value = message
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                _errorMessage.value = null

                when (val result = repository.login(email, password)) {
                    is Result.Success -> {
                        val userId = result.data.userId ?: result.data.profile?.id
                        if (userId != null) {
                            _currentUser.value = repository.getCurrentUser()
                            _authState.value = AuthState.Authenticated(userId)
                        } else {
                            _authState.value = AuthState.Error(
                                StringResources.getString(R.string.error_user_profile_not_found)
                            )
                        }
                    }

                    is Result.Error -> {
                        _errorMessage.value = result.message
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                val message = getErrorMessage(e)
                _errorMessage.value = message
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                _errorMessage.value = null

                when (val result = repository.loginAsGuest()) {
                    is Result.Success -> {
                        _currentUser.value = repository.getCurrentUser()
                        _authState.value = AuthState.Guest(result.data)
                    }

                    is Result.Error -> {
                        _errorMessage.value = result.message
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                val message = getErrorMessage(e)
                _errorMessage.value = message
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        _errorMessage.value = null
    }

    fun logoutGuest() {
        viewModelScope.launch {
            try {
                repository.logoutGuest()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun isGuestMode(): Boolean = repository.isGuestMode()

    fun logoutCurrentSession() {
        if (repository.isGuestMode()) {
            logoutGuest()
        } else {
            logout()
        }
    }

    fun updateProfile(displayName: String?, avatarUrl: String?) {
        viewModelScope.launch {
            try {
                _isUpdatingProfile.value = true
                _profileSaved.value = false
                _errorMessage.value = null

                when (val result = repository.updateProfile(displayName, avatarUrl)) {
                    is Result.Success -> {
                        _currentUser.value = repository.getCurrentUser()
                        _profileSaved.value = true
                    }

                    is Result.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isUpdatingProfile.value = false
            }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            try {
                _isDeletingAccount.value = true
                _accountDeleted.value = false
                _errorMessage.value = null

                when (val result = repository.deleteAccount(password)) {
                    is Result.Success -> {
                        _currentUser.value = null
                        _authState.value = AuthState.Unauthenticated
                        _accountDeleted.value = true
                    }

                    is Result.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isDeletingAccount.value = false
            }
        }
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUser.value = repository.getCurrentUser()
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun consumeProfileSaved() {
        _profileSaved.value = false
    }

    fun consumeAccountDeleted() {
        _accountDeleted.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearAuthError() {
        _errorMessage.value = null
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
