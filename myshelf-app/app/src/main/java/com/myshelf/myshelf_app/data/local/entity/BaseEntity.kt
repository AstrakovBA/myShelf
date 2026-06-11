package com.myshelf.myshelf_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/**
 * Базовая Room-сущность с общими полями для всех локальных таблиц.
 * Наследники должны быть помечены аннотацией @Entity.
 */
abstract class BaseEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    open var id: Long = 0

    @ColumnInfo(name = "created_at")
    open var createdAt: Long = System.currentTimeMillis()

    @ColumnInfo(name = "updated_at")
    open var updatedAt: Long = System.currentTimeMillis()

    fun touchUpdatedAt() {
        updatedAt = System.currentTimeMillis()
    }
}
