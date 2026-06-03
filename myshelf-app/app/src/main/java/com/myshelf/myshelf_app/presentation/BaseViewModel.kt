package com.myshelf.myshelf_app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.util.Resource
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
            is IOException -> "Ошибка сети. Проверьте подключение к интернету."

            is SocketTimeoutException -> "Превышено время ожидания ответа сервера."

            is HttpException -> when (throwable.code()) {
                400 -> "Некорректный запрос."
                401 -> "Не авторизован. Войдите в аккаунт."
                403 -> "Доступ запрещён."
                404 -> "Ресурс не найден."
                409 -> "Конфликт данных. Попробуйте синхронизировать."
                500 -> "Ошибка сервера. Попробуйте позже."
                else -> "Ошибка сервера: ${throwable.code()}"
            }

            else -> throwable.localizedMessage?.takeIf { it.isNotBlank() }
                ?: "Произошла неизвестная ошибка"
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
