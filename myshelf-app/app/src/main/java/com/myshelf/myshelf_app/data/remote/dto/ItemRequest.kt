package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ItemRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("category")
    val category: String,

    @SerializedName("season")
    val season: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null
)
