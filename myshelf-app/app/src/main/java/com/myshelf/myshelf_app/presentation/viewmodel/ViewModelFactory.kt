package com.myshelf.myshelf_app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myshelf.myshelf_app.data.remote.RetrofitClient
import com.myshelf.myshelf_app.data.repository.RepositoryProvider

class ViewModelFactory(
    private val context: Context,
    private val userId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appContext = context.applicationContext
        return when {
            modelClass.isAssignableFrom(ItemsViewModel::class.java) -> {
                val id = userId ?: RetrofitClient.getTokenManager(appContext).getUserId()
                    ?: throw IllegalStateException("userId required for ItemsViewModel")
                ItemsViewModel(
                    repository = RepositoryProvider.itemsRepository(appContext),
                    userId = id
                ) as T
            }

            modelClass.isAssignableFrom(OutfitsViewModel::class.java) -> {
                val id = userId ?: RetrofitClient.getTokenManager(appContext).getUserId()
                    ?: throw IllegalStateException("userId required for OutfitsViewModel")
                OutfitsViewModel(
                    repository = RepositoryProvider.outfitsRepository(appContext),
                    userId = id
                ) as T
            }

            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(
                    repository = RepositoryProvider.authRepository(appContext)
                ) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    repository = RepositoryProvider.settingsRepository(appContext)
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
