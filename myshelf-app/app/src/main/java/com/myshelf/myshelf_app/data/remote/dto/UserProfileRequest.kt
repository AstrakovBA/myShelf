package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserProfileRequest(
    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)
