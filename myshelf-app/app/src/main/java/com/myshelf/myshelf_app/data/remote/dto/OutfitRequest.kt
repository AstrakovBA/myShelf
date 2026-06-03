package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OutfitRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("season")
    val season: String? = null,

    @SerializedName("slots")
    val slots: List<OutfitSlotRequest>
)
