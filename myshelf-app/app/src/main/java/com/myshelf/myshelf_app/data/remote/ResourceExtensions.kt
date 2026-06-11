package com.myshelf.myshelf_app.data.remote

import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.Result
import com.myshelf.myshelf_app.util.StringResources

fun <T> Resource<T>.toResultUnit(): Result<Unit> = when (this) {
    is Resource.Success -> Result.Success(Unit)
    is Resource.Error -> Result.Error(message)
    is Resource.Loading -> Result.Error(StringResources.getString(R.string.error_invalid_loading_state))
}

fun <T> Resource<T>.toResult(): Result<T> = when (this) {
    is Resource.Success -> Result.Success(data)
    is Resource.Error -> Result.Error(message)
    is Resource.Loading -> Result.Error(StringResources.getString(R.string.error_invalid_loading_state))
}
