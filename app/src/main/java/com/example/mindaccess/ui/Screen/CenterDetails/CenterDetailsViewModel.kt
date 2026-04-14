package com.example.mindaccess.ui.Screen.CenterDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Repository.CenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CenterDetailsViewModel @Inject constructor(
    private val repository: CenterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val centerName: String = checkNotNull(savedStateHandle["centerName"])

    private val _center = MutableStateFlow<CenterModel?>(null)
    val center: StateFlow<CenterModel?> = _center.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchCenterDetails()
    }

    private fun fetchCenterDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.getCenters()
                .onSuccess { centers ->
                    _center.value = centers.find { it.name == centerName }
                }
                .onFailure { _errorMessage.value = it.message ?: "An unknown error occurred" }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
