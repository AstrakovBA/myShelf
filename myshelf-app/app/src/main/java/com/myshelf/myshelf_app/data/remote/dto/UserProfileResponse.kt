package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)
