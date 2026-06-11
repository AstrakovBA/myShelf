package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ItemResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("category")
    val category: String,

    @SerializedName("season")
    val season: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
