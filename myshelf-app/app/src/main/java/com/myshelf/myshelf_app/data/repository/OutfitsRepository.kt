package com.myshelf.myshelf_app.data.repository

import com.myshelf.myshelf_app.data.local.dao.OutfitDao
import com.myshelf.myshelf_app.data.local.dao.OutfitSlotDao
import com.myshelf.myshelf_app.data.local.entity.OutfitLocal
import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal
import com.myshelf.myshelf_app.data.local.relation.OutfitWithSlots
import com.myshelf.myshelf_app.data.mapper.OutfitMapper
import com.myshelf.myshelf_app.data.mapper.OutfitMapper.isLocalOnlyId
import com.myshelf.myshelf_app.data.mapper.OutfitMapper.toLocal
import com.myshelf.myshelf_app.data.mapper.OutfitMapper.toRequest
import com.myshelf.myshelf_app.data.mapper.OutfitMapper.withServerData
import com.myshelf.myshelf_app.data.mapper.OutfitMapper.toLocal as slotToLocal
import com.myshelf.myshelf_app.data.remote.ApiCallHandler
import com.myshelf.myshelf_app.data.remote.ApiException
import com.myshelf.myshelf_app.data.remote.TokenManager
import com.myshelf.myshelf_app.data.remote.WardrobeApiService
import com.myshelf.myshelf_app.data.remote.authorizationHeader
import com.myshelf.myshelf_app.data.remote.toResultUnit
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.Result
import com.myshelf.myshelf_app.util.StringResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class OutfitsRepository(
    private val outfitDao: OutfitDao,
    private val outfitSlotDao: OutfitSlotDao,
    private val apiService: WardrobeApiService,
    private val tokenManager: TokenManager
) {

    fun getOutfitsFlow(userId: String): Flow<List<OutfitLocal>> {
        return outfitDao.getOutfitsByUser(userId)
    }

    fun getOutfitWithSlotsFlow(outfitId: String): Flow<OutfitWithSlots> {
        return outfitDao.getOutfitWithSlots(outfitId)
    }

    fun getOutfitsWithSlotsFlow(userId: String): Flow<List<OutfitWithSlots>> {
        return flow {
            outfitDao.getOutfitsByUser(userId).collect { outfits ->
                val withSlots = outfits.map { outfit ->
                    OutfitWithSlots(
                        outfit = outfit,
                        slots = outfitSlotDao.getSlotsByOutfitId(outfit.id)
                    )
                }
                emit(withSlots)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createOutfit(
        outfit: OutfitLocal,
        slots: List<OutfitSlotLocal>
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val localOutfit = outfit.copy(
                isDirty = true,
                updatedAt = now,
                createdAt = if (outfit.createdAt > 0) outfit.createdAt else now
            )
            val localSlots = slots.map { slot ->
                slot.copy(
                    outfitId = localOutfit.id,
                    id = slot.id.ifBlank { OutfitMapper.generateSlotId() }
                )
            }

            outfitDao.upsertOutfit(localOutfit)
            outfitSlotDao.deleteByOutfitId(localOutfit.id)
            outfitSlotDao.insertAll(localSlots)

            if (!tokenManager.isLoggedIn()) {
                return Result.Success(Unit)
            }

            runCatching { uploadDirtyOutfits(listOf(localOutfit to localSlots)) }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: StringResources.getString(R.string.error_save_outfit), e)
        }
    }

    suspend fun syncOutfitsWithServer(userId: String): Result<Unit> {
        if (tokenManager.isGuestMode()) {
            return Result.Error(StringResources.getString(R.string.error_sync_guest_mode))
        }
        if (!tokenManager.isLoggedIn()) {
            return Result.Error(StringResources.getString(R.string.error_sync_auth_required))
        }

        return try {
            val dirtyForUser = outfitDao.getDirtyOutfits().filter { it.userId == userId }
            val dirtyWithSlots = dirtyForUser.map { outfit ->
                outfit to outfitSlotDao.getSlotsByOutfitId(outfit.id)
            }
            uploadDirtyOutfits(dirtyWithSlots)

            val authHeader = tokenManager.authorizationHeader()
            when (val fetchResult = ApiCallHandler.safeApiCall {
                apiService.getOutfits(authHeader)
            }) {
                is Resource.Success -> {
                    outfitDao.deleteAllByUser(userId)
                    fetchResult.data.forEach { dto ->
                        val localOutfit = dto.toLocal(userId, isDirty = false)
                        outfitDao.upsertOutfit(localOutfit)
                        outfitSlotDao.deleteByOutfitId(localOutfit.id)
                        val slots = dto.slots.map { it.slotToLocal(localOutfit.id) }
                        if (slots.isNotEmpty()) {
                            outfitSlotDao.insertAll(slots)
                        }
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

    suspend fun updateOutfit(
        outfitId: String,
        name: String,
        description: String?,
        season: String?,
        slots: List<OutfitSlotLocal>
    ): Result<Unit> {
        return try {
            val existing = outfitDao.getOutfitById(outfitId)
                ?: return Result.Error(StringResources.getString(R.string.error_outfit_not_found))

            val updatedOutfit = existing.copy(
                name = name,
                description = description,
                season = season,
                isDirty = true,
                updatedAt = System.currentTimeMillis()
            )
            val updatedSlots = slots.map { slot ->
                slot.copy(
                    outfitId = outfitId,
                    id = slot.id.ifBlank { OutfitMapper.generateSlotId() }
                )
            }

            outfitDao.upsertOutfit(updatedOutfit)
            outfitSlotDao.deleteByOutfitId(outfitId)
            outfitSlotDao.insertAll(updatedSlots)

            if (!tokenManager.isLoggedIn()) {
                return Result.Success(Unit)
            }

            runCatching { uploadDirtyOutfits(listOf(updatedOutfit to updatedSlots)) }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: StringResources.getString(R.string.error_update_outfit), e)
        }
    }

    suspend fun clearLocalCache(userId: String) {
        outfitDao.deleteAllByUser(userId)
    }

    suspend fun deleteOutfit(outfitId: String): Result<Unit> {
        return try {
            if (tokenManager.isLoggedIn() && !isLocalOnlyId(outfitId)) {
                val authHeader = tokenManager.authorizationHeader()
                val deleteResult = ApiCallHandler.safeApiCall {
                    apiService.deleteOutfit(authHeader, outfitId)
                }.toResultUnit()

                if (deleteResult is Result.Error) {
                    return deleteResult
                }
            }

            outfitSlotDao.deleteByOutfitId(outfitId)
            outfitDao.deleteOutfitById(outfitId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: StringResources.getString(R.string.error_delete_outfit), e)
        }
    }

    private suspend fun uploadDirtyOutfits(dirtyOutfits: List<Pair<OutfitLocal, List<OutfitSlotLocal>>>) {
        if (dirtyOutfits.isEmpty()) return

        val authHeader = tokenManager.authorizationHeader()

        for ((outfit, slots) in dirtyOutfits) {
            val request = outfit.toRequest(slots)

            if (isLocalOnlyId(outfit.id)) {
                val response = apiService.createOutfit(authHeader, request)
                if (!response.isSuccessful) {
                    throw ApiException(
                        response.code(),
                        ApiCallHandler.httpErrorMessage(response)
                    )
                }
                val created = ApiCallHandler.requireSuccess(response)
                val oldId = outfit.id
                outfitDao.deleteOutfitById(oldId)
                outfitSlotDao.deleteByOutfitId(oldId)

                val syncedOutfit = created.toLocal(outfit.userId, isDirty = false)
                outfitDao.upsertOutfit(syncedOutfit)
                val syncedSlots = created.slots.map { it.slotToLocal(syncedOutfit.id) }
                if (syncedSlots.isNotEmpty()) {
                    outfitSlotDao.insertAll(syncedSlots)
                }
            } else {
                val response = apiService.updateOutfit(authHeader, outfit.id, request)
                if (response.isSuccessful) {
                    val updated = response.body()
                    val syncedOutfit = if (updated != null) {
                        outfit.withServerData(updated)
                    } else {
                        outfit.copy(isDirty = false, updatedAt = System.currentTimeMillis())
                    }
                    outfitDao.upsertOutfit(syncedOutfit)
                    outfitSlotDao.deleteByOutfitId(syncedOutfit.id)
                    val syncedSlots = updated?.slots?.map { it.slotToLocal(syncedOutfit.id) }
                        ?: slots
                    if (syncedSlots.isNotEmpty()) {
                        outfitSlotDao.insertAll(syncedSlots)
                    }
                } else if (response.code() == 404) {
                    val createResponse = apiService.createOutfit(authHeader, request)
                    if (!createResponse.isSuccessful) {
                        throw ApiException(
                            createResponse.code(),
                            ApiCallHandler.httpErrorMessage(createResponse)
                        )
                    }
                    val created = ApiCallHandler.requireSuccess(createResponse)
                    outfitDao.deleteOutfitById(outfit.id)
                    outfitSlotDao.deleteByOutfitId(outfit.id)
                    val syncedOutfit = created.toLocal(outfit.userId, isDirty = false)
                    outfitDao.upsertOutfit(syncedOutfit)
                    val syncedSlots = created.slots.map { it.slotToLocal(syncedOutfit.id) }
                    if (syncedSlots.isNotEmpty()) {
                        outfitSlotDao.insertAll(syncedSlots)
                    }
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
