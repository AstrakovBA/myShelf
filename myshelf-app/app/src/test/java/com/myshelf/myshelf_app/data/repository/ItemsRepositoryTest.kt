package com.myshelf.myshelf_app.data.repository

import com.myshelf.myshelf_app.data.local.dao.ItemDao
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.remote.TokenManager
import com.myshelf.myshelf_app.data.remote.WardrobeApiService
import com.myshelf.myshelf_app.data.remote.authorizationHeader
import com.myshelf.myshelf_app.data.remote.dto.ItemResponse
import com.myshelf.myshelf_app.domain.model.Category
import com.myshelf.myshelf_app.util.StringResources
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ItemsRepositoryTest {

    private val itemDao: ItemDao = mockk()
    private val apiService: WardrobeApiService = mockk()
    private val tokenManager: TokenManager = mockk()
    private lateinit var repository: ItemsRepository

    private val userId = "user-1"

    @Before
    fun setUp() {
        repository = ItemsRepository(itemDao, apiService, tokenManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getItemsFlow returns flow from dao`() = runTest {
        val items = listOf(sampleItem("item-1"))
        every { itemDao.getItemsByUser(userId) } returns flowOf(items)

        val result = repository.getItemsFlow(userId).first()

        assertEquals(items, result)
    }

    @Test
    fun `syncItemsWithServer uploads dirty items and downloads fresh data`() = runTest {
        val dirtyItem = sampleItem(
            id = "local_dirty",
            isDirty = true
        )
        val serverItem = ItemResponse(
            id = "server-1",
            userId = userId,
            name = "Synced Shirt",
            category = Category.TOP.name
        )

        coEvery { tokenManager.isLoggedIn() } returns true
        every { tokenManager.authorizationHeader() } returns "Bearer token"
        coEvery { itemDao.getDirtyItems() } returns listOf(dirtyItem)
        coEvery { apiService.createItem(any(), any()) } returns Response.success(serverItem)
        coEvery { itemDao.deleteItemById(dirtyItem.id) } just runs
        coEvery { itemDao.upsertItem(any()) } just runs
        coEvery { apiService.getItems(any()) } returns Response.success(listOf(serverItem))
        coEvery { itemDao.deleteAllByUser(userId) } just runs
        coEvery { itemDao.upsertItems(any()) } just runs

        val result = repository.syncItemsWithServer(userId)

        assertTrue(result.isSuccess)
        coVerify { apiService.createItem(any(), any()) }
        coVerify { itemDao.deleteAllByUser(userId) }
        coVerify { itemDao.upsertItems(any()) }
    }

    @Test
    fun `createItem saves locally with isDirty=true`() = runTest {
        val capturedItem = slot<ItemLocal>()
        coEvery { itemDao.upsertItem(capture(capturedItem)) } just runs
        coEvery { tokenManager.isLoggedIn() } returns false

        val input = sampleItem(id = "local_new", isDirty = false)
        val result = repository.createItem(input)

        assertTrue(result.isSuccess)
        assertTrue(capturedItem.captured.isDirty)
        assertEquals(userId, capturedItem.captured.userId)
        assertEquals("Blue Shirt", capturedItem.captured.name)
        coVerify(exactly = 1) { itemDao.upsertItem(any()) }
    }

    @Test
    fun `syncItemsWithServer returns error when user is not logged in`() = runTest {
        mockkObject(StringResources)
        every { StringResources.getString(any()) } returns "Auth required"
        coEvery { tokenManager.isLoggedIn() } returns false

        val result = repository.syncItemsWithServer(userId)

        assertTrue(result.isError)
        coVerify(exactly = 0) { itemDao.getDirtyItems() }
    }

    private fun sampleItem(
        id: String = "item-1",
        isDirty: Boolean = false
    ) = ItemLocal(
        id = id,
        userId = userId,
        name = "Blue Shirt",
        description = "Cotton",
        category = Category.TOP.name,
        season = "SUMMER",
        isDirty = isDirty
    )
}
