package com.myshelf.myshelf_app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Добавляет JWT в заголовок Authorization, если запрос не публичный
 * и заголовок ещё не задан явно. При 401 сбрасывает сессию.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val request = when {
            isPublicEndpoint(path) -> originalRequest
            originalRequest.header(HEADER_AUTHORIZATION) != null -> originalRequest
            else -> {
                val bearerToken = tokenManager.getBearerToken()
                if (bearerToken == null) {
                    originalRequest
                } else {
                    originalRequest.newBuilder()
                        .header(HEADER_AUTHORIZATION, bearerToken)
                        .build()
                }
            }
        }

        val response = chain.proceed(request)
        if (response.code == 401 && !isPublicEndpoint(path)) {
            tokenManager.clearSession()
            tokenManager.notifySessionExpired()
        }
        return response
    }

    private fun isPublicEndpoint(path: String): Boolean {
        return PUBLIC_PATH_SUFFIXES.any { path.endsWith(it) }
    }

    companion object {
        const val HEADER_AUTHORIZATION = "Authorization"

        private val PUBLIC_PATH_SUFFIXES = listOf(
            "/auth/register",
            "/auth/login"
        )
    }
}
