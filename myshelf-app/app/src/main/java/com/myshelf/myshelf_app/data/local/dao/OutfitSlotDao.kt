package com.myshelf.myshelf_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal

@Dao
interface OutfitSlotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slot: OutfitSlotLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<OutfitSlotLocal>)

    @Query("DELETE FROM outfit_slots WHERE outfit_id = :outfitId")
    suspend fun deleteByOutfitId(outfitId: String)

    @Query("DELETE FROM outfit_slots WHERE id = :slotId")
    suspend fun deleteById(slotId: String)
}
