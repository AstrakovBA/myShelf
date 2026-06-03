package com.myshelf.myshelf_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outfit_slots",
    foreignKeys = [
        ForeignKey(
            entity = OutfitLocal::class,
            parentColumns = ["id"],
            childColumns = ["outfit_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemLocal::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["outfit_id"]),
        Index(value = ["item_id"])
    ]
)
data class OutfitSlotLocal(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "outfit_id")
    val outfitId: String,

    @ColumnInfo(name = "item_id")
    val itemId: String? = null,

    @ColumnInfo(name = "slot_type")
    val slotType: String
)
