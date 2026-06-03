package com.myshelf.myshelf_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.myshelf.myshelf_app.data.local.entity.OutfitLocal
import com.myshelf.myshelf_app.data.local.relation.OutfitWithSlots
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {

    @Query("SELECT * FROM outfits WHERE user_id = :userId ORDER BY created_at DESC")
    fun getOutfitsByUser(userId: String): Flow<List<OutfitLocal>>

    @Transaction
    @Query("SELECT * FROM outfits WHERE id = :outfitId")
    fun getOutfitWithSlots(outfitId: String): Flow<OutfitWithSlots>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOutfit(outfit: OutfitLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOutfits(outfits: List<OutfitLocal>)

    @Query("DELETE FROM outfits WHERE id = :outfitId")
    suspend fun deleteOutfitById(outfitId: String)

    @Query("SELECT * FROM outfits WHERE id = :outfitId LIMIT 1")
    suspend fun getOutfitById(outfitId: String): OutfitLocal?

    @Query("SELECT * FROM outfits WHERE is_dirty = 1")
    suspend fun getDirtyOutfits(): List<OutfitLocal>

    @Query("DELETE FROM outfits WHERE user_id = :userId")
    suspend fun deleteAllByUser(userId: String)
}
