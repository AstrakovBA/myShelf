package com.myshelf.myshelf_app.data.remote

import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.Result

fun <T> Resource<T>.toResult(): Result<T> = when (this) {
    is Resource.Success -> Result.Success(data)
    is Resource.Error -> Result.Error(message)
    is Resource.Loading -> Result.Error("Недопустимое состояние загрузки")
}

fun <T> Resource<T>.toResultUnit(): Result<Unit> = when (this) {
    is Resource.Success -> Result.Success(Unit)
    is Resource.Error -> Result.Error(message)
    is Resource.Loading -> Result.Error("Недопустимое состояние загрузки")
}
