package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserRegistrationRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("displayName")
    val displayName: String
)
