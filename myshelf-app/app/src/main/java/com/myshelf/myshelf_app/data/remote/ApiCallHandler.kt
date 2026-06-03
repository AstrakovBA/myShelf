package com.myshelf.myshelf_app.data.remote

import com.myshelf.myshelf_app.util.Resource
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiCallHandler {

    suspend fun <T> safeApiCall(
        call: suspend () -> Response<T>
    ): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                when {
                    body != null -> Resource.Success(body)
                    response.code() == 204 -> {
                        @Suppress("UNCHECKED_CAST")
                        Resource.Success(Unit as T)
                    }
                    else -> Resource.Error("Пустой ответ сервера")
                }
            } else {
                Resource.Error(mapHttpError(response))
            }
        } catch (e: UnknownHostException) {
            Resource.Error("Ошибка сети. Проверьте подключение к интернету.")
        } catch (e: SocketTimeoutException) {
            Resource.Error("Превышено время ожидания ответа сервера.")
        } catch (e: IOException) {
            Resource.Error("Ошибка сети: ${e.localizedMessage ?: "неизвестная ошибка"}")
        } catch (e: ApiException) {
            Resource.Error(e.message ?: "Ошибка API")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Произошла неизвестная ошибка")
        }
    }

    fun <T> Response<T>.toResource(): Resource<T> {
        return if (isSuccessful) {
            val body = body()
            if (body != null || code() == 204) {
                @Suppress("UNCHECKED_CAST")
                Resource.Success(body as T)
            } else {
                Resource.Error("Пустой ответ сервера")
            }
        } else {
            Resource.Error(mapHttpError(this))
        }
    }

    fun <T> requireSuccess(response: Response<T>): T {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) return body
            if (response.code() == 204) {
                @Suppress("UNCHECKED_CAST")
                return Unit as T
            }
            throw ApiException(response.code(), "Пустой ответ сервера", response.errorBody()?.string())
        }
        throw ApiException(response.code(), mapHttpError(response), response.errorBody()?.string())
    }

    fun <T> httpErrorMessage(response: Response<T>): String = mapHttpError(response)

    private fun <T> mapHttpError(response: Response<T>): String {
        val serverMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        val baseMessage = when (response.code()) {
            400 -> "Некорректный запрос"
            401 -> "Не авторизован. Войдите в аккаунт"
            403 -> "Доступ запрещён"
            404 -> "Ресурс не найден"
            409 -> "Конфликт данных"
            422 -> "Ошибка валидации данных"
            500 -> "Ошибка сервера"
            else -> "Ошибка сервера: ${response.code()}"
        }
        return if (serverMessage != null) "$baseMessage: $serverMessage" else baseMessage
    }
}
