package com.myshelf.myshelf_app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.data.local.entity.OutfitLocal
import com.myshelf.myshelf_app.data.local.entity.OutfitSlotLocal
import com.myshelf.myshelf_app.data.local.relation.OutfitWithSlots
import com.myshelf.myshelf_app.data.mapper.OutfitMapper
import com.myshelf.myshelf_app.data.repository.OutfitsRepository
import com.myshelf.myshelf_app.presentation.BaseViewModel
import com.myshelf.myshelf_app.presentation.outfit.OutfitUpdates
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OutfitsViewModel(
    private val repository: OutfitsRepository,
    private val userId: String
) : BaseViewModel() {

    private val _outfits = MutableStateFlow<Resource<List<OutfitWithSlots>>>(Resource.Loading)
    val outfits: StateFlow<Resource<List<OutfitWithSlots>>> = _outfits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _outfitSaved = MutableStateFlow(false)
    val outfitSaved: StateFlow<Boolean> = _outfitSaved.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var loadJob: Job? = null

    fun loadOutfits() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _outfits.value = Resource.Loading
                _errorMessage.value = null

                repository.getOutfitsWithSlotsFlow(userId).collect { list ->
                    _outfits.value = Resource.Success(list)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val message = getErrorMessage(e)
                _errorMessage.value = message
                _outfits.value = Resource.Error(message)
            }
        }
    }

    fun createOutfit(
        name: String,
        description: String?,
        season: String?,
        slots: List<OutfitSlotLocal>,
        outfitId: String = OutfitMapper.generateLocalId()
    ) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _outfitSaved.value = false
                _errorMessage.value = null

                val outfit = OutfitLocal(
                    id = outfitId,
                    userId = userId,
                    name = name,
                    description = description,
                    season = season,
                    isDirty = true
                )

                val slotsWithOutfitId = slots.map { slot ->
                    slot.copy(outfitId = outfitId)
                }

                when (val result = repository.createOutfit(outfit, slotsWithOutfitId)) {
                    is Result.Success -> {
                        _errorMessage.value = null
                        _outfitSaved.value = true
                    }

                    is Result.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun consumeOutfitSaved() {
        _outfitSaved.value = false
    }

    fun loadOutfit(outfitId: String): Flow<OutfitWithSlots?> {
        return repository.getOutfitWithSlotsFlow(outfitId)
    }

    fun syncOutfits() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                _errorMessage.value = null

                when (val result = repository.syncOutfitsWithServer(userId)) {
                    is Result.Success -> _errorMessage.value = null
                    is Result.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deleteOutfit(outfitId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                when (val result = repository.deleteOutfit(outfitId)) {
                    is Result.Success -> _errorMessage.value = null
                    is Result.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOutfit(outfitId: String, updates: OutfitUpdates) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _outfitSaved.value = false
                _errorMessage.value = null

                val slots = updates.slots
                    .filter { (_, itemId) -> !itemId.isNullOrBlank() }
                    .map { (slotType, itemId) ->
                        OutfitSlotLocal(
                            id = OutfitMapper.generateSlotId(),
                            outfitId = outfitId,
                            itemId = itemId,
                            slotType = slotType.name
                        )
                    }

                when (
                    val result = repository.updateOutfit(
                        outfitId = outfitId,
                        name = updates.name,
                        description = updates.description,
                        season = updates.season?.name,
                        slots = slots
                    )
                ) {
                    is Result.Success -> {
                        _errorMessage.value = null
                        _outfitSaved.value = true
                    }

                    is Result.Error -> _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = getErrorMessage(e)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}
