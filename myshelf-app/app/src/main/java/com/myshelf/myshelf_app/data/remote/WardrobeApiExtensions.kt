package com.myshelf.myshelf_app.data.remote

import com.myshelf.myshelf_app.data.remote.dto.AuthRequest
import com.myshelf.myshelf_app.data.remote.dto.AuthResponse
import retrofit2.Response

/** Вход по [AuthRequest] (email + password). */
suspend fun WardrobeApiService.login(request: AuthRequest): Response<AuthResponse> {
    return login(email = request.email, password = request.password)
}

/** Формирует заголовок Authorization для явной передачи в API. */
fun TokenManager.authorizationHeader(): String {
    return getBearerToken()
        ?: throw IllegalStateException("JWT токен отсутствует. Выполните вход в аккаунт.")
}
