package com.myshelf.myshelf_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE user_id = :userId ORDER BY created_at DESC")
    fun getItemsByUser(userId: String): Flow<List<ItemLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ItemLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ItemLocal>)

    @Query("SELECT * FROM items WHERE is_dirty = 1")
    suspend fun getDirtyItems(): List<ItemLocal>

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): ItemLocal?

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    @Query("DELETE FROM items WHERE user_id = :userId")
    suspend fun deleteAllByUser(userId: String)
}
