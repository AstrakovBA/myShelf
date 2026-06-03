package com.myshelf.myshelf_app.data.repository

import com.myshelf.myshelf_app.data.local.dao.UserDao
import com.myshelf.myshelf_app.data.local.entity.UserLocal
import com.myshelf.myshelf_app.data.remote.ApiCallHandler
import com.myshelf.myshelf_app.data.remote.TokenManager
import com.myshelf.myshelf_app.data.remote.WardrobeApiService
import com.myshelf.myshelf_app.data.remote.authorizationHeader
import com.myshelf.myshelf_app.data.remote.dto.AuthRequest
import com.myshelf.myshelf_app.data.remote.dto.AuthResponse
import com.myshelf.myshelf_app.data.remote.dto.PasswordChangeRequest
import com.myshelf.myshelf_app.data.remote.dto.UserProfileRequest
import com.myshelf.myshelf_app.data.remote.dto.UserRegistrationRequest
import com.myshelf.myshelf_app.data.remote.login
import com.myshelf.myshelf_app.data.remote.toResultUnit
import com.myshelf.myshelf_app.util.Result

class AuthRepository(
    private val apiService: WardrobeApiService,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) {

    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthResponse> {
        val request = UserRegistrationRequest(
            email = email,
            password = password,
            displayName = displayName
        )
        return when (val resource = ApiCallHandler.safeApiCall { apiService.register(request) }) {
            is com.myshelf.myshelf_app.util.Resource.Success -> {
                persistAuth(resource.data)
                Result.Success(resource.data)
            }

            is com.myshelf.myshelf_app.util.Resource.Error -> Result.Error(resource.message)

            else -> Result.Error("Ошибка регистрации")
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        val request = AuthRequest(email = email, password = password)
        return when (val resource = ApiCallHandler.safeApiCall { apiService.login(request) }) {
            is com.myshelf.myshelf_app.util.Resource.Success -> {
                persistAuth(resource.data)
                Result.Success(resource.data)
            }

            is com.myshelf.myshelf_app.util.Resource.Error -> Result.Error(resource.message)

            else -> Result.Error("Ошибка входа")
        }
    }

    suspend fun updateProfile(displayName: String?, avatarUrl: String?): Result<Unit> {
        if (!tokenManager.isLoggedIn()) {
            return Result.Error("Требуется авторизация")
        }

        return try {
            val authHeader = tokenManager.authorizationHeader()
            val request = UserProfileRequest(
                displayName = displayName,
                avatarUrl = avatarUrl
            )
            when (val resource = ApiCallHandler.safeApiCall {
                apiService.updateProfile(authHeader, request)
            }) {
                is com.myshelf.myshelf_app.util.Resource.Success -> {
                    val profile = resource.data
                    val userId = tokenManager.getUserId()
                    if (userId != null) {
                        val existing = userDao.getUserById(userId)
                        if (existing != null) {
                            userDao.update(
                                existing.copy(
                                    displayName = profile.displayName,
                                    avatarUrl = profile.avatarUrl
                                )
                            )
                        }
                    }
                    Result.Success(Unit)
                }

                is com.myshelf.myshelf_app.util.Resource.Error -> Result.Error(resource.message)

                else -> Result.Error("Не удалось обновить профиль")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Не удалось обновить профиль", e)
        }
    }

    suspend fun changePassword(oldPass: String, newPass: String): Result<Unit> {
        if (!tokenManager.isLoggedIn()) {
            return Result.Error("Требуется авторизация")
        }

        return try {
            val authHeader = tokenManager.authorizationHeader()
            val request = PasswordChangeRequest(oldPassword = oldPass, newPassword = newPass)
            ApiCallHandler.safeApiCall {
                apiService.changePassword(authHeader, request)
            }.toResultUnit()
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Не удалось сменить пароль", e)
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun getUserId(): String? = tokenManager.getUserId()

    suspend fun getCurrentUser(): UserLocal? {
        val userId = tokenManager.getUserId() ?: return null
        return userDao.getUserById(userId)
    }

    fun logout() {
        tokenManager.clearSession()
    }

    private suspend fun persistAuth(response: AuthResponse) {
        val profile = response.profile
        val userId = response.userId ?: profile?.id
        if (userId == null) {
            throw IllegalStateException("Сервер не вернул идентификатор пользователя")
        }

        tokenManager.saveAuthSession(
            token = response.token,
            userId = userId,
            email = profile?.email
        )

        if (profile != null) {
            userDao.insert(
                UserLocal(
                    id = userId,
                    email = profile.email,
                    displayName = profile.displayName,
                    avatarUrl = profile.avatarUrl
                )
            )
        }
    }
}
