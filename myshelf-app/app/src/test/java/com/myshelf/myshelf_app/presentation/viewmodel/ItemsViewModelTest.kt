package com.myshelf.myshelf_app.presentation.viewmodel

import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.repository.ItemsRepository
import com.myshelf.myshelf_app.domain.model.Category
import com.myshelf.myshelf_app.util.MainDispatcherRule
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userId = "user-1"
    private val repository: ItemsRepository = mockk()
    private lateinit var viewModel: ItemsViewModel

    private val topItem = ItemLocal(
        id = "item-top",
        userId = userId,
        name = "Blue Shirt",
        category = Category.TOP.name
    )

    private val bottomItem = ItemLocal(
        id = "item-bottom",
        userId = userId,
        name = "Jeans",
        category = Category.BOTTOM.name
    )

    @Test
    fun `loadItems emits success state when repository returns items`() = runTest {
        every { repository.getItemsFlow(userId) } returns flowOf(listOf(topItem, bottomItem))
        viewModel = ItemsViewModel(repository, userId)

        viewModel.loadItems()

        val state = viewModel.items.value
        assertTrue(state is Resource.Success)
        assertEquals(2, (state as Resource.Success).data.size)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `createItem calls repository with correct data`() = runTest {
        val capturedItem = slot<ItemLocal>()
        coEvery { repository.createItem(capture(capturedItem)) } returns Result.Success(Unit)
        viewModel = ItemsViewModel(repository, userId)

        viewModel.createItem(
            name = "Blue Shirt",
            description = "Cotton",
            category = Category.TOP.name,
            season = "SUMMER",
            imageUrl = "https://example.com/shirt.jpg"
        )

        coVerify(exactly = 1) { repository.createItem(any()) }
        assertEquals("Blue Shirt", capturedItem.captured.name)
        assertEquals("Cotton", capturedItem.captured.description)
        assertEquals(Category.TOP.name, capturedItem.captured.category)
        assertEquals("SUMMER", capturedItem.captured.season)
        assertEquals("https://example.com/shirt.jpg", capturedItem.captured.imageUrl)
        assertEquals(userId, capturedItem.captured.userId)
        assertTrue(capturedItem.captured.isDirty)
        assertTrue(viewModel.itemSaved.value)
    }

    @Test
    fun `deleteItem calls repository with item id`() = runTest {
        val itemId = "item-to-delete"
        coEvery { repository.deleteItem(itemId) } returns Result.Success(Unit)
        viewModel = ItemsViewModel(repository, userId)

        viewModel.deleteItem(itemId)

        coVerify(exactly = 1) { repository.deleteItem(itemId) }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `filterByCategory filters items correctly`() = runTest {
        every { repository.getItemsFlow(userId) } returns flowOf(listOf(topItem, bottomItem))
        viewModel = ItemsViewModel(repository, userId)
        viewModel.loadItems()

        viewModel.filterByCategory(Category.TOP.name)

        val state = viewModel.items.value
        assertTrue(state is Resource.Success)
        val filtered = (state as Resource.Success).data
        assertEquals(1, filtered.size)
        assertEquals(Category.TOP.name, filtered.first().category)
    }

    @Test
    fun `search filters items by name`() = runTest {
        every { repository.getItemsFlow(userId) } returns flowOf(listOf(topItem, bottomItem))
        viewModel = ItemsViewModel(repository, userId)
        viewModel.loadItems()

        viewModel.search("jeans")

        val state = viewModel.items.value as Resource.Success
        assertEquals(1, state.data.size)
        assertEquals("Jeans", state.data.first().name)
    }
}
