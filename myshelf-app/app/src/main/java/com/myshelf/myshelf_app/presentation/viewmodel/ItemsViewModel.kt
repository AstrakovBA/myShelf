package com.myshelf.myshelf_app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.myshelf.myshelf_app.data.local.entity.ItemLocal
import com.myshelf.myshelf_app.data.mapper.ItemMapper
import com.myshelf.myshelf_app.data.repository.ItemsRepository
import com.myshelf.myshelf_app.presentation.BaseViewModel
import com.myshelf.myshelf_app.util.Resource
import com.myshelf.myshelf_app.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemsViewModel(
    private val repository: ItemsRepository,
    private val userId: String
) : BaseViewModel() {

    private val _items = MutableStateFlow<Resource<List<ItemLocal>>>(Resource.Loading)
    val items: StateFlow<Resource<List<ItemLocal>>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val allItems = mutableListOf<ItemLocal>()
    private var categoryFilter: String? = null
    private var searchQuery: String = ""
    private var loadJob: Job? = null

    fun loadItems() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _items.value = Resource.Loading
                _errorMessage.value = null

                repository.getItemsFlow(userId).collect { list ->
                    allItems.clear()
                    allItems.addAll(list)
                    _items.value = Resource.Success(applyFilters(allItems))
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val message = getErrorMessage(e)
                _errorMessage.value = message
                _items.value = Resource.Error(message)
            }
        }
    }

    fun createItem(
        name: String,
        description: String?,
        category: String,
        season: String?,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val item = ItemLocal(
                    id = ItemMapper.generateLocalId(),
                    userId = userId,
                    name = name,
                    description = description,
                    category = category,
                    season = season,
                    imageUrl = imageUrl,
                    isDirty = true
                )

                when (val result = repository.createItem(item)) {
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

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                when (val result = repository.deleteItem(itemId)) {
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

    fun syncItems() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                when (val result = repository.syncItemsWithServer(userId)) {
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

    fun filterByCategory(category: String?) {
        categoryFilter = category?.takeIf { it.isNotBlank() }
        publishFilteredItems()
    }

    fun search(query: String) {
        searchQuery = query.trim()
        publishFilteredItems()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun publishFilteredItems() {
        if (allItems.isEmpty() && _items.value is Resource.Loading) return
        _items.value = Resource.Success(applyFilters(allItems))
    }

    private fun applyFilters(source: List<ItemLocal>): List<ItemLocal> {
        return source
            .asSequence()
            .filter { item ->
                categoryFilter == null || item.category.equals(categoryFilter, ignoreCase = true)
            }
            .filter { item ->
                searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.description?.contains(searchQuery, ignoreCase = true) == true
            }
            .toList()
    }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}
