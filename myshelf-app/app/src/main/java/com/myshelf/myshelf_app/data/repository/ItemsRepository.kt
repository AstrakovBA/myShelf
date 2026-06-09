package com.myshelf.myshelf_app.data.repository

import com.myshelf.myshelf_app.data.local.dao.ItemDao
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.mapper.ItemMapper.isLocalOnlyId
import com.myshelf.myshelf_app.data.mapper.ItemMapper.toLocal
import com.myshelf.myshelf_app.data.mapper.ItemMapper.toRequest
import com.myshelf.myshelf_app.data.mapper.ItemMapper.withServerData
import com.myshelf.myshelf_app.data.remote.ApiCallHandler
import com.myshelf.myshelf_app.data.remote.ApiException
import com.myshelf.myshelf_app.data.remote.TokenManager
import com.myshelf.myshelf_app.data.remote.WardrobeApiService
import com.myshelf.myshelf_app.data.remote.authorizationHeader
import com.myshelf.myshelf_app.data.remote.toResultUnit
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.presentation.item.ItemUpdates
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.Result
import com.myshelf.myshelf_app.util.StringResources
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class ItemsRepository(
    private val itemDao: ItemDao,
    private val apiService: WardrobeApiService,
    private val tokenManager: TokenManager
) {

    fun getItemsFlow(userId: String): Flow<List<ItemLocal>> {
        return itemDao.getItemsByUser(userId)
    }

    fun getItemFlow(itemId: String): Flow<ItemLocal?> {
        return itemDao.observeItemById(itemId)
    }

    suspend fun createItem(item: ItemLocal): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val localItem = item.copy(
                isDirty = true,
                updatedAt = now,
                createdAt = if (item.createdAt > 0) item.createdAt else now
            )
            itemDao.upsertItem(localItem)

            if (!tokenManager.isLoggedIn()) {
                return Result.Success(Unit)
            }

            runCatching { uploadDirtyItems(listOf(localItem)) }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(
                message = e.localizedMessage ?: StringResources.getString(R.string.error_save_item),
                cause = e
            )
        }
    }

    suspend fun syncItemsWithServer(userId: String): Result<Unit> {
        if (!tokenManager.isLoggedIn()) {
            return Result.Error(StringResources.getString(R.string.error_sync_auth_required))
        }

        return try {
            val dirtyForUser = itemDao.getDirtyItems().filter { it.userId == userId }
            uploadDirtyItems(dirtyForUser)

            val authHeader = tokenManager.authorizationHeader()
            when (val fetchResult = ApiCallHandler.safeApiCall {
                apiService.getItems(authHeader)
            }) {
                is Resource.Success -> {
                    val serverItems = fetchResult.data.map { dto ->
                        dto.toLocal(userId = userId, isDirty = false)
                    }
                    itemDao.deleteAllByUser(userId)
                    if (serverItems.isNotEmpty()) {
                        itemDao.upsertItems(serverItems)
                    }
                    Result.Success(Unit)
                }

                is Resource.Error -> Result.Error(fetchResult.message)

                is Resource.Loading -> Result.Error(StringResources.getString(R.string.error_sync_failed))
            }
        } catch (e: IOException) {
            Result.Error(StringResources.getString(R.string.error_network_sync_retry), e)
        } catch (e: ApiException) {
            Result.Error(e.message ?: StringResources.getString(R.string.error_api), e)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: StringResources.getString(R.string.error_sync_failed), e)
        }
    }

    suspend fun updateItem(itemId: String, updates: ItemUpdates): Result<Unit> {
        return try {
            val existing = itemDao.getItemById(itemId)
                ?: return Result.Error(StringResources.getString(R.string.error_item_not_found))

            val now = System.currentTimeMillis()
            val updatedItem = existing.copy(
                name = updates.name,
                description = updates.description,
                category = updates.category.name,
                season = updates.season?.name,
                imageUrl = updates.imageUrl,
                isDirty = true,
                updatedAt = now
            )
            itemDao.upsertItem(updatedItem)

            if (!tokenManager.isLoggedIn()) {
                return Result.Success(Unit)
            }

            runCatching { uploadDirtyItems(listOf(updatedItem)) }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(
                message = e.localizedMessage ?: StringResources.getString(R.string.error_update_item),
                cause = e
            )
        }
    }

    suspend fun clearLocalCache(userId: String) {
        itemDao.deleteAllByUser(userId)
    }

    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            val existing = itemDao.getItemById(itemId)

            if (tokenManager.isLoggedIn() && existing != null && !isLocalOnlyId(itemId)) {
                val authHeader = tokenManager.authorizationHeader()
                val deleteResult = ApiCallHandler.safeApiCall {
                    apiService.deleteItem(authHeader, itemId)
                }.toResultUnit()

                if (deleteResult is Result.Error) {
                    return deleteResult
                }
            }

            itemDao.deleteItemById(itemId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: StringResources.getString(R.string.error_delete_item), e)
        }
    }

    private suspend fun uploadDirtyItems(dirtyItems: List<ItemLocal>) {
        if (dirtyItems.isEmpty()) return

        val authHeader = tokenManager.authorizationHeader()

        for (item in dirtyItems) {
            val request = item.toRequest()

            if (isLocalOnlyId(item.id)) {
                val response = apiService.createItem(authHeader, request)
                if (!response.isSuccessful) {
                    throw ApiException(
                        response.code(),
                        ApiCallHandler.httpErrorMessage(response)
                    )
                }
                val created = ApiCallHandler.requireSuccess(response)
                itemDao.deleteItemById(item.id)
                itemDao.upsertItem(created.toLocal(item.userId, isDirty = false))
            } else {
                val response = apiService.updateItem(authHeader, item.id, request)
                if (response.isSuccessful) {
                    val updated = response.body()
                    val synced = if (updated != null) {
                        item.withServerData(updated)
                    } else {
                        item.copy(isDirty = false, updatedAt = System.currentTimeMillis())
                    }
                    itemDao.upsertItem(synced)
                } else if (response.code() == 404) {
                    val createResponse = apiService.createItem(authHeader, request)
                    if (!createResponse.isSuccessful) {
                        throw ApiException(
                            createResponse.code(),
                            ApiCallHandler.httpErrorMessage(createResponse)
                        )
                    }
                    val created = ApiCallHandler.requireSuccess(createResponse)
                    itemDao.deleteItemById(item.id)
                    itemDao.upsertItem(created.toLocal(item.userId, isDirty = false))
                } else {
                    throw ApiException(
                        response.code(),
                        ApiCallHandler.httpErrorMessage(response)
                    )
                }
            }
        }
    }
}
