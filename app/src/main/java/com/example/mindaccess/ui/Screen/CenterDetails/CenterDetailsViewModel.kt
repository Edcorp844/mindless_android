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

    init {
        fetchCenterDetails()
    }

    private fun fetchCenterDetails() {
        viewModelScope.launch {
            repository.getCenters().collect { centers ->
                _center.value = centers.find { it.name == centerName }
            }
        }
    }
}
