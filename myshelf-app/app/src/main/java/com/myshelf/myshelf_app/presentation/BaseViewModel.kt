package com.myshelf.myshelf_app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.StringResources
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseViewModel : ViewModel() {

    protected fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException,
            is IOException -> StringResources.getString(R.string.error_network_check_connection)

            is SocketTimeoutException -> StringResources.getString(R.string.error_server_timeout)

            is HttpException -> when (throwable.code()) {
                400 -> StringResources.getString(R.string.error_http_bad_request)
                401 -> StringResources.getString(R.string.error_http_unauthorized)
                403 -> StringResources.getString(R.string.error_http_forbidden)
                404 -> StringResources.getString(R.string.error_http_not_found)
                409 -> StringResources.getString(R.string.error_http_conflict)
                500 -> StringResources.getString(R.string.error_http_server)
                else -> StringResources.getString(R.string.error_http_code, throwable.code())
            }

            else -> throwable.localizedMessage?.takeIf { it.isNotBlank() }
                ?: StringResources.getString(R.string.error_unknown)
        }
    }

    protected fun launchSafe(
        onError: (String) -> Unit = {},
        block: suspend () -> Unit
    ): Job {
        return viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                onError(getErrorMessage(throwable))
            }
        ) {
            block()
        }
    }

    protected fun <T> launchResource(
        stateFlow: MutableStateFlow<Resource<T>>,
        block: suspend () -> T
    ): Job {
        return viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                stateFlow.value = Resource.Error(getErrorMessage(throwable))
            }
        ) {
            stateFlow.value = Resource.Loading
            val result = block()
            stateFlow.value = Resource.Success(result)
        }
    }

    protected fun <T> mutableResourceState(): MutableStateFlow<Resource<T>> =
        MutableStateFlow(Resource.Loading)

    protected fun <T> MutableStateFlow<Resource<T>>.asResourceState(): StateFlow<Resource<T>> =
        asStateFlow()
}
