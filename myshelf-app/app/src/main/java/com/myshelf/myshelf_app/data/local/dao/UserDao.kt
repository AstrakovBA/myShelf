package com.myshelf.myshelf_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.myshelf.myshelf_app.data.local.entity.UserLocal

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserLocal)

    @Update
    suspend fun update(user: UserLocal)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserLocal?

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: String)
}
