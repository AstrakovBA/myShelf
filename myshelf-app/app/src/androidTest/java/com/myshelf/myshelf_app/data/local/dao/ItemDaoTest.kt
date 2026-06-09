package com.myshelf.myshelf_app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myshelf.myshelf_app.data.local.AppDatabase
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.local.entity.UserLocal
import com.myshelf.myshelf_app.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        itemDao = database.itemDao()
        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertItem_insertsItemToDatabase() = runTest {
        insertUser("user-1")

        val item = sampleItem(id = "item-1", userId = "user-1")
        itemDao.upsertItem(item)

        val stored = itemDao.getItemById("item-1")
        assertNotNull(stored)
        assertEquals(item.name, stored?.name)
        assertEquals(item.category, stored?.category)
    }

    @Test
    fun getItemsByUser_returnsItemsForSpecificUser() = runTest {
        insertUser("user-1")
        insertUser("user-2")

        itemDao.upsertItem(sampleItem(id = "item-1", userId = "user-1", name = "Shirt"))
        itemDao.upsertItem(sampleItem(id = "item-2", userId = "user-2", name = "Pants"))

        val userOneItems = itemDao.getItemsByUser("user-1").first()

        assertEquals(1, userOneItems.size)
        assertEquals("user-1", userOneItems.first().userId)
        assertEquals("Shirt", userOneItems.first().name)
    }

    @Test
    fun getDirtyItems_returnsOnlyDirtyItems() = runTest {
        insertUser("user-1")
        itemDao.upsertItem(sampleItem(id = "clean", userId = "user-1", isDirty = false))
        itemDao.upsertItem(sampleItem(id = "dirty", userId = "user-1", isDirty = true))

        val dirtyItems = itemDao.getDirtyItems()

        assertEquals(1, dirtyItems.size)
        assertEquals("dirty", dirtyItems.first().id)
        assertTrue(dirtyItems.first().isDirty)
    }

    @Test
    fun deleteItemById_removesItemFromDatabase() = runTest {
        insertUser("user-1")
        itemDao.upsertItem(sampleItem(id = "item-1", userId = "user-1"))

        itemDao.deleteItemById("item-1")

        assertEquals(null, itemDao.getItemById("item-1"))
    }

    private suspend fun insertUser(userId: String) {
        userDao.insert(
            UserLocal(
                id = userId,
                email = "$userId@test.com",
                displayName = "Test User"
            )
        )
    }

    private fun sampleItem(
        id: String,
        userId: String,
        name: String = "Blue Shirt",
        isDirty: Boolean = false
    ) = ItemLocal(
        id = id,
        userId = userId,
        name = name,
        category = Category.TOP.name,
        isDirty = isDirty
    )
}
