package com.myshelf.myshelf_app.util

/**
 * Обёртка для представления состояния асинхронной операции в UI.
 */
sealed class Resource<out T> {

    data object Loading : Resource<Nothing>()

    data class Success<T>(val data: T) : Resource<T>()

    data class Error(val message: String) : Resource<Nothing>()

    val isLoading: Boolean
        get() = this is Loading

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun errorOrNull(): String? = (this as? Error)?.message
}
