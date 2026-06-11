package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("profile")
    val profile: UserProfileResponse? = null
) {
    /** Идентификатор пользователя из профиля (совместимость с серверным AuthResponse). */
    val userId: String?
        get() = profile?.id
}
