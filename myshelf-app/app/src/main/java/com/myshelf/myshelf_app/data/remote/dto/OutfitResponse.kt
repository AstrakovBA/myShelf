package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OutfitResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("season")
    val season: String? = null,

    @SerializedName("slots")
    val slots: List<OutfitSlotResponse> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String? = null
)
