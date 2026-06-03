package com.myshelf.myshelf_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myshelf.myshelf_app.data.local.dao.ItemDao
import com.myshelf.myshelf_app.data.local.dao.OutfitDao
import com.myshelf.myshelf_app.data.local.dao.OutfitSlotDao
import com.myshelf.myshelf_app.data.local.dao.UserDao
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.local.entity.OutfitLocal
import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal
import com.myshelf.myshelf_app.data.local.entity.UserLocal

@Database(
    entities = [
        UserLocal::class,
        ItemLocal::class,
        OutfitLocal::class,
        OutfitSlotLocal::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun itemDao(): ItemDao

    abstract fun outfitDao(): OutfitDao

    abstract fun outfitSlotDao(): OutfitSlotDao

    companion object {
        private const val DATABASE_NAME = "myshelf.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}
