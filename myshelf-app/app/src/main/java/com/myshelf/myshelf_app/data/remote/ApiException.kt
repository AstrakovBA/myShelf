package com.myshelf.myshelf_app.data.remote

class ApiException(
    val code: Int,
    override val message: String,
    val errorBody: String? = null
) : Exception(message)
