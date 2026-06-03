package com.myshelf.myshelf_app.util

sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun errorOrNull(): String? = (this as? Error)?.message
}
