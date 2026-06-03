package com.myshelf.myshelf_app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.myshelf.myshelf_app.data.local.entity.OutfitLocal
import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal

data class OutfitWithSlots(
    @Embedded
    val outfit: OutfitLocal,

    @Relation(
        parentColumn = "id",
        entityColumn = "outfit_id"
    )
    val slots: List<OutfitSlotLocal>
)
