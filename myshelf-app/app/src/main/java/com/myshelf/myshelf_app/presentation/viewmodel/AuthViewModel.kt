package com.myshelf.myshelf_app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.data.local.entity.UserLocal
import com.myshelf.myshelf_app.data.repository.AuthRepository
import com.myshelf.myshelf_app.presentation.BaseViewModel
import com.myshelf.myshelf_app.presentation.auth.AuthState
import com.myshelf.myshelf_app.util.Result
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

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                if (repository.isLoggedIn()) {
                    val user = repository.getCurrentUser()
                    _currentUser.value = user
                    val userId = repository.getUserId()
                    if (userId != null) {
                        _authState.value = AuthState.Authenticated(userId)
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
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
                            _authState.value = AuthState.Error("Не удалось получить профиль пользователя")
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
                            _authState.value = AuthState.Error("Не удалось получить профиль пользователя")
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

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        _errorMessage.value = null
    }

    fun updateProfile(displayName: String?, avatarUrl: String?) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                when (val result = repository.updateProfile(displayName, avatarUrl)) {
                    is Result.Success -> {
                        _currentUser.value = repository.getCurrentUser()
                    }

                    is Result.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /** Временный вход без сервера для навигационных заглушек. */
    fun loginAsGuest(userId: String = "guest-user") {
        _currentUser.value = null
        _authState.value = AuthState.Authenticated(userId)
        _errorMessage.value = null
    }
}
