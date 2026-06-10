package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PasswordConfirmRequest(
    @SerializedName("currentPassword")
    val currentPassword: String
)
