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
        slots: List<OutfitSlotLocal>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val outfit = OutfitLocal(
                    id = OutfitMapper.generateLocalId(),
                    userId = userId,
                    name = name,
                    description = description,
                    season = season,
                    isDirty = true
                )

                when (val result = repository.createOutfit(outfit, slots)) {
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
                _isLoading.value = true
                _errorMessage.value = null

                when (
                    val result = repository.updateOutfit(
                        outfitId = outfitId,
                        name = updates.name,
                        description = updates.description,
                        season = updates.season,
                        slots = updates.slots
                    )
                ) {
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

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}
