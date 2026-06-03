package com.myshelf.myshelf_app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Добавляет JWT в заголовок Authorization, если запрос не публичный
 * и заголовок ещё не задан явно.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (isPublicEndpoint(path)) {
            return chain.proceed(originalRequest)
        }

        if (originalRequest.header(HEADER_AUTHORIZATION) != null) {
            return chain.proceed(originalRequest)
        }

        val bearerToken = tokenManager.getBearerToken()
            ?: return chain.proceed(originalRequest)

        val authenticatedRequest = originalRequest.newBuilder()
            .header(HEADER_AUTHORIZATION, bearerToken)
            .build()

        return chain.proceed(authenticatedRequest)
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
