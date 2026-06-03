package com.myshelf.myshelf_app.data.repository

import android.content.Context
import com.myshelf.myshelf_app.data.local.AppDatabase
import com.myshelf.myshelf_app.data.remote.RetrofitClient

/**
 * Фабрика репозиториев (до внедрения Hilt/Koin).
 */
object RepositoryProvider {

    @Volatile
    private var itemsRepository: ItemsRepository? = null

    @Volatile
    private var outfitsRepository: OutfitsRepository? = null

    @Volatile
    private var authRepository: AuthRepository? = null

    @Volatile
    private var settingsRepository: SettingsRepository? = null

    fun itemsRepository(context: Context): ItemsRepository {
        return itemsRepository ?: synchronized(this) {
            itemsRepository ?: createItemsRepository(context).also { itemsRepository = it }
        }
    }

    fun outfitsRepository(context: Context): OutfitsRepository {
        return outfitsRepository ?: synchronized(this) {
            outfitsRepository ?: createOutfitsRepository(context).also { outfitsRepository = it }
        }
    }

    fun authRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: createAuthRepository(context).also { authRepository = it }
        }
    }

    fun settingsRepository(context: Context): SettingsRepository {
        return settingsRepository ?: synchronized(this) {
            settingsRepository ?: SettingsRepository(context.applicationContext)
                .also { settingsRepository = it }
        }
    }

    private fun createItemsRepository(context: Context): ItemsRepository {
        val appContext = context.applicationContext
        RetrofitClient.init(appContext)
        val db = AppDatabase.getInstance(appContext)
        return ItemsRepository(
            itemDao = db.itemDao(),
            apiService = RetrofitClient.makeApiService(),
            tokenManager = RetrofitClient.getTokenManager(appContext)
        )
    }

    private fun createOutfitsRepository(context: Context): OutfitsRepository {
        val appContext = context.applicationContext
        RetrofitClient.init(appContext)
        val db = AppDatabase.getInstance(appContext)
        return OutfitsRepository(
            outfitDao = db.outfitDao(),
            outfitSlotDao = db.outfitSlotDao(),
            apiService = RetrofitClient.makeApiService(),
            tokenManager = RetrofitClient.getTokenManager(appContext)
        )
    }

    private fun createAuthRepository(context: Context): AuthRepository {
        val appContext = context.applicationContext
        RetrofitClient.init(appContext)
        val db = AppDatabase.getInstance(appContext)
        return AuthRepository(
            apiService = RetrofitClient.makeApiService(),
            tokenManager = RetrofitClient.getTokenManager(appContext),
            userDao = db.userDao()
        )
    }
}
