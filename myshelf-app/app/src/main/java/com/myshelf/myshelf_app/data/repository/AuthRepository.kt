package com.myshelf.myshelf_app.data.repository

import com.myshelf.myshelf_app.data.local.dao.ItemDao
import com.myshelf.myshelf_app.data.local.dao.OutfitDao
import com.myshelf.myshelf_app.data.local.dao.UserDao
import com.myshelf.myshelf_app.data.local.entity.UserLocal
import com.myshelf.myshelf_app.util.Constants
import java.util.UUID
import com.myshelf.myshelf_app.data.remote.ApiCallHandler
import com.myshelf.myshelf_app.data.remote.TokenManager
import com.myshelf.myshelf_app.data.remote.WardrobeApiService
import com.myshelf.myshelf_app.data.remote.authorizationHeader
import com.myshelf.myshelf_app.data.remote.dto.AuthRequest
import com.myshelf.myshelf_app.data.remote.dto.AuthResponse
import com.myshelf.myshelf_app.data.remote.dto.PasswordChangeRequest
import com.myshelf.myshelf_app.data.remote.dto.PasswordConfirmRequest
import com.myshelf.myshelf_app.data.remote.dto.UserProfileRequest
import com.myshelf.myshelf_app.data.remote.dto.UserRegistrationRequest
import com.myshelf.myshelf_app.data.remote.login
import com.myshelf.myshelf_app.data.remote.toResultUnit
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.util.Result
import com.myshelf.myshelf_app.util.StringResources

class AuthRepository(
    private val apiService: WardrobeApiService,
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
    private val itemDao: ItemDao,
    private val outfitDao: OutfitDao,
    private val settingsRepository: SettingsRepository
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

            else -> Result.Error(StringResources.getString(R.string.error_register))
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

            else -> Result.Error(StringResources.getString(R.string.error_login))
        }
    }

    suspend fun updateProfile(displayName: String?, avatarUrl: String?): Result<Unit> {
        if (!tokenManager.isLoggedIn()) {
            return Result.Error(StringResources.getString(R.string.error_auth_required))
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

                else -> Result.Error(StringResources.getString(R.string.error_update_profile))
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: StringResources.getString(R.string.error_update_profile), e)
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        if (!tokenManager.isLoggedIn()) {
            return Result.Error(StringResources.getString(R.string.error_auth_required))
        }

        val userId = tokenManager.getUserId()
            ?: return Result.Error(StringResources.getString(R.string.error_auth_required))

        return try {
            val authHeader = tokenManager.authorizationHeader()
            val request = PasswordConfirmRequest(currentPassword = password)
            when (
                val resource = ApiCallHandler.safeApiCall {
                    apiService.deleteAccount(authHeader, request)
                }.toResultUnit()
            ) {
                is Result.Success -> {
                    clearLocalUserData(userId)
                    settingsRepository.clearAll()
                    tokenManager.clearSession()
                    Result.Success(Unit)
                }

                is Result.Error -> resource
            }
        } catch (e: Exception) {
            Result.Error(
                e.localizedMessage ?: StringResources.getString(R.string.error_delete_account),
                e
            )
        }
    }

    suspend fun changePassword(oldPass: String, newPass: String): Result<Unit> {
        if (!tokenManager.isLoggedIn()) {
            return Result.Error(StringResources.getString(R.string.error_auth_required))
        }

        return try {
            val authHeader = tokenManager.authorizationHeader()
            val request = PasswordChangeRequest(oldPassword = oldPass, newPassword = newPass)
            ApiCallHandler.safeApiCall {
                apiService.changePassword(authHeader, request)
            }.toResultUnit()
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: StringResources.getString(R.string.error_change_password), e)
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun isGuestMode(): Boolean = tokenManager.isGuestMode()

    fun getUserId(): String? = tokenManager.getUserId()

    suspend fun loginAsGuest(): Result<String> {
        return try {
            if (tokenManager.isGuestMode()) {
                logoutGuest()
            }
            val guestId = UUID.randomUUID().toString()
            tokenManager.saveGuestSession(guestId)
            userDao.insert(
                UserLocal(
                    id = guestId,
                    email = Constants.GUEST_EMAIL,
                    displayName = StringResources.getString(R.string.guest_display_name)
                )
            )
            Result.Success(guestId)
        } catch (e: Exception) {
            Result.Error(
                e.localizedMessage ?: StringResources.getString(R.string.error_guest_login),
                e
            )
        }
    }

    suspend fun logoutGuest() {
        val guestId = tokenManager.getUserId() ?: return
        if (!tokenManager.isGuestMode()) return

        itemDao.deleteAllByUser(guestId)
        outfitDao.deleteAllByUser(guestId)
        userDao.deleteById(guestId)
        tokenManager.clearSession()
    }

    suspend fun getCurrentUser(): UserLocal? {
        val userId = tokenManager.getUserId() ?: return null
        return userDao.getUserById(userId)
    }

    fun logout() {
        tokenManager.clearSession()
    }

    private suspend fun clearLocalUserData(userId: String) {
        itemDao.deleteAllByUser(userId)
        outfitDao.deleteAllByUser(userId)
        userDao.deleteById(userId)
    }

    private suspend fun clearGuestDataIfPresent() {
        if (!tokenManager.isGuestMode()) return
        val guestId = tokenManager.getUserId() ?: return
        itemDao.deleteAllByUser(guestId)
        outfitDao.deleteAllByUser(guestId)
        userDao.deleteById(guestId)
        tokenManager.clearSession()
    }

    private suspend fun persistAuth(response: AuthResponse) {
        clearGuestDataIfPresent()
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
