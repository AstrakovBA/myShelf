package com.myshelf.myshelf_app.data.mapper

import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.mapper.ItemMapper.isLocalOnlyId
import com.myshelf.myshelf_app.data.mapper.ItemMapper.toLocal
import com.myshelf.myshelf_app.data.mapper.ItemMapper.toRequest
import com.myshelf.myshelf_app.data.remote.dto.ItemRequest
import com.myshelf.myshelf_app.data.remote.dto.ItemResponse
import com.myshelf.myshelf_app.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemMapperTest {

    @Test
    fun `toRequest maps item fields`() {
        val item = ItemLocal(
            id = "item-1",
            userId = "user-1",
            name = "Coat",
            description = "Warm",
            category = Category.OUTERWEAR.name,
            season = "WINTER",
            imageUrl = "https://example.com/coat.jpg"
        )

        val request: ItemRequest = item.toRequest()

        assertEquals("Coat", request.name)
        assertEquals("Warm", request.description)
        assertEquals(Category.OUTERWEAR.name, request.category)
        assertEquals("WINTER", request.season)
        assertEquals("https://example.com/coat.jpg", request.imageUrl)
    }

    @Test
    fun `toLocal maps server response`() {
        val response = ItemResponse(
            id = "server-99",
            name = "Hat",
            category = Category.HEADWEAR.name
        )

        val local = response.toLocal(userId = "user-1", isDirty = false)

        assertEquals("server-99", local.id)
        assertEquals("user-1", local.userId)
        assertEquals("Hat", local.name)
        assertFalse(local.isDirty)
    }

    @Test
    fun `isLocalOnlyId detects local ids`() {
        assertTrue(isLocalOnlyId("local_${java.util.UUID.randomUUID()}"))
        assertFalse(isLocalOnlyId("server-123"))
    }
}
