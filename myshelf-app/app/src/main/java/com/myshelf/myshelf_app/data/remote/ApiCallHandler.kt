package com.myshelf.myshelf_app.data.remote

import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.StringResources
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
                    else -> Resource.Error(StringResources.getString(R.string.error_empty_server_response))
                }
            } else {
                Resource.Error(mapHttpError(response))
            }
        } catch (e: UnknownHostException) {
            Resource.Error(StringResources.getString(R.string.error_network_check_connection))
        } catch (e: SocketTimeoutException) {
            Resource.Error(StringResources.getString(R.string.error_server_timeout))
        } catch (e: IOException) {
            Resource.Error(
                StringResources.getString(
                    R.string.error_network_unknown,
                    e.localizedMessage ?: StringResources.getString(R.string.error_unknown)
                )
            )
        } catch (e: ApiException) {
            Resource.Error(e.message ?: StringResources.getString(R.string.error_api))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: StringResources.getString(R.string.error_unknown))
        }
    }

    fun <T> Response<T>.toResource(): Resource<T> {
        return if (isSuccessful) {
            val body = body()
            if (body != null || code() == 204) {
                @Suppress("UNCHECKED_CAST")
                Resource.Success(body as T)
            } else {
                Resource.Error(StringResources.getString(R.string.error_empty_server_response))
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
            throw ApiException(
                response.code(),
                StringResources.getString(R.string.error_empty_server_response),
                response.errorBody()?.string()
            )
        }
        throw ApiException(response.code(), mapHttpError(response), response.errorBody()?.string())
    }

    fun <T> httpErrorMessage(response: Response<T>): String = mapHttpError(response)

    private fun <T> mapHttpError(response: Response<T>): String {
        val serverMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        val baseMessage = when (response.code()) {
            400 -> StringResources.getString(R.string.error_http_bad_request)
            401 -> StringResources.getString(R.string.error_http_unauthorized)
            403 -> StringResources.getString(R.string.error_http_forbidden)
            404 -> StringResources.getString(R.string.error_http_not_found)
            409 -> StringResources.getString(R.string.error_http_conflict)
            422 -> StringResources.getString(R.string.error_http_validation)
            500 -> StringResources.getString(R.string.error_http_server)
            else -> StringResources.getString(R.string.error_http_code, response.code())
        }
        return if (serverMessage != null) "$baseMessage: $serverMessage" else baseMessage
    }
}
