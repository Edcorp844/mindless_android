package com.example.mindaccess.ui.Screen.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Model.CenterCategory
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Repository.CenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CenterRepository
) : ViewModel() {

    private val _centers = MutableStateFlow<List<CenterModel>>(emptyList())
    
    private val _selectedCategory = MutableStateFlow<CenterCategory?>(null)
    val selectedCategory: StateFlow<CenterCategory?> = _selectedCategory.asStateFlow()

    val centers: StateFlow<List<CenterModel>> = combine(_centers, _selectedCategory) { centers, selected ->
        if (selected == null) centers
        else centers.filter { it.category.id == selected.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CenterCategory>> = _centers.map { centers ->
        centers.map { it.category }.distinctBy { it.id }.sortedBy { it.label }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchCenters()
    }

    private fun fetchCenters() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCenters()
                .onSuccess {
                    _centers.value = it
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "An unknown error occurred"
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectCategory(category: CenterCategory?) {
        _selectedCategory.value = category
    }
}
