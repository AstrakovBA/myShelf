package com.myshelf.myshelf_app.data.mapper

import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.remote.dto.ItemRequest
import com.myshelf.myshelf_app.data.remote.dto.ItemResponse
import java.time.Instant

object ItemMapper {

    fun ItemLocal.toRequest(): ItemRequest = ItemRequest(
        name = name,
        description = description,
        category = category,
        season = season,
        imageUrl = imageUrl
    )

    fun ItemResponse.toLocal(userId: String, isDirty: Boolean = false): ItemLocal = ItemLocal(
        id = id,
        userId = userId,
        name = name,
        description = description,
        category = category,
        season = season,
        imageUrl = imageUrl,
        isDirty = isDirty,
        createdAt = parseTimestamp(createdAt),
        updatedAt = parseTimestamp(updatedAt)
    )

    fun ItemLocal.withServerData(response: ItemResponse): ItemLocal = copy(
        id = response.id,
        name = response.name,
        description = response.description,
        category = response.category,
        season = response.season,
        imageUrl = response.imageUrl,
        isDirty = false,
        createdAt = parseTimestamp(response.createdAt, createdAt),
        updatedAt = parseTimestamp(response.updatedAt, updatedAt)
    )

    fun isLocalOnlyId(id: String): Boolean = id.startsWith(LOCAL_ID_PREFIX)

    fun generateLocalId(): String = "$LOCAL_ID_PREFIX${java.util.UUID.randomUUID()}"

    private fun parseTimestamp(value: String?, fallback: Long = System.currentTimeMillis()): Long {
        if (value.isNullOrBlank()) return fallback
        return runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(fallback)
    }

    private const val LOCAL_ID_PREFIX = "local_"
}
