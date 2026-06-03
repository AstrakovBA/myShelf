package com.myshelf.myshelf_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OutfitSlotResponse(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("slotType")
    val slotType: String,

    @SerializedName("itemId")
    val itemId: String? = null
)
