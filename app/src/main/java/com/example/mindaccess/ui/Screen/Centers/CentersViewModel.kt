package com.example.mindaccess.ui.Screen.Centers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Repository.CenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CentersViewModel @Inject constructor(
    private val repository: CenterRepository
) : ViewModel() {

    private val _centers = MutableStateFlow<List<CenterModel>>(emptyList())
    val centers: StateFlow<List<CenterModel>> = _centers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val categories: StateFlow<List<String>> = _centers.map { centers ->
        val dynamicCategories = centers.map { it.category }.distinct().sorted()
        listOf("All") + dynamicCategories
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    init {
        getCenters()
    }

    fun getCenters() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCenters().collect { centerList ->
                _centers.value = centerList
                _isLoading.value = false
            }
        }
    }
}
