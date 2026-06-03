package com.myshelf.myshelf_app.data.mapper

import com.myshelf.myshelf_app.data.local.entity.OutfitLocal
import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal
import com.myshelf.myshelf_app.data.remote.dto.OutfitRequest
import com.myshelf.myshelf_app.data.remote.dto.OutfitResponse
import com.myshelf.myshelf_app.data.remote.dto.OutfitSlotRequest
import com.myshelf.myshelf_app.data.remote.dto.OutfitSlotResponse
import java.time.Instant
import java.util.UUID

object OutfitMapper {

    fun OutfitLocal.toRequest(slots: List<OutfitSlotLocal>): OutfitRequest = OutfitRequest(
        name = name,
        description = description,
        season = season,
        slots = slots.map { it.toRequest() }
    )

    fun OutfitSlotLocal.toRequest(): OutfitSlotRequest = OutfitSlotRequest(
        slotType = slotType,
        itemId = itemId
    )

    fun OutfitResponse.toLocal(userId: String, isDirty: Boolean = false): OutfitLocal = OutfitLocal(
        id = id,
        userId = userId,
        name = name,
        description = description,
        season = season,
        createdAt = parseTimestamp(createdAt),
        isDirty = isDirty,
        updatedAt = System.currentTimeMillis()
    )

    fun OutfitSlotResponse.toLocal(outfitId: String): OutfitSlotLocal = OutfitSlotLocal(
        id = id ?: UUID.randomUUID().toString(),
        outfitId = outfitId,
        itemId = itemId,
        slotType = slotType
    )

    fun OutfitLocal.withServerData(response: OutfitResponse): OutfitLocal = copy(
        id = response.id,
        name = response.name,
        description = response.description,
        season = response.season,
        isDirty = false,
        createdAt = parseTimestamp(response.createdAt, createdAt),
        updatedAt = System.currentTimeMillis()
    )

    fun isLocalOnlyId(id: String): Boolean = id.startsWith(LOCAL_ID_PREFIX)

    fun generateLocalId(): String = "$LOCAL_ID_PREFIX${UUID.randomUUID()}"

    fun generateSlotId(): String = "$LOCAL_ID_PREFIX${UUID.randomUUID()}"

    private fun parseTimestamp(value: String?, fallback: Long = System.currentTimeMillis()): Long {
        if (value.isNullOrBlank()) return fallback
        return runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(fallback)
    }

    private const val LOCAL_ID_PREFIX = "local_"
}
