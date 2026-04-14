package com.example.mindaccess.ui.Screen.Centers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Repository.CenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CentersViewModel @Inject constructor(
    private val repository: CenterRepository
) : ViewModel() {

    private val _centers = MutableStateFlow<List<CenterModel>>(emptyList())
    val centers = _centers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Translated logic: Extract unique labels for categories
    val categories: StateFlow<List<String>> = _centers
        .map { centersList ->
            val extracted = centersList.map { it.category.label }.toSet()
            listOf("All") + extracted.sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Translated logic: Filtered centers based on selected category and search query
    val filteredCenters: StateFlow<List<CenterModel>> = combine(_centers, _selectedCategory, _searchQuery) { centers, category, query ->
        centers.filter { center ->
            val matchesCategory = if (category == "All") true else center.category.label == category
            val matchesSearch = if (query.isEmpty()) true else center.name.contains(query, ignoreCase = true) || 
                               (center.description?.contains(query, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchCenters()
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchCenters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.getCenters()
                .onSuccess { _centers.value = it }
                .onFailure { _errorMessage.value = it.message ?: "An unknown error occurred" }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
