package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PasswordChangeRequest(
    @SerializedName("currentPassword")
    val oldPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)
