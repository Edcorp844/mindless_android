package com.example.mindaccess.ui.Screen.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Model.AppNotification
import com.example.mindaccess.Domain.Model.CenterCategory
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Repository.CenterRepository
import com.example.mindaccess.Domain.Repository.UserRepository
import com.example.mindaccess.utils.ErrorMapper
import com.example.mindaccess.utils.NetworkObserver
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val repository: CenterRepository,
    private val userRepository: UserRepository,
    private val networkObserver: NetworkObserver
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

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
        observeNetwork()
        refresh()
        observeNotifications()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkObserver.observe.collect { status ->
                if (status == NetworkObserver.Status.Available && _centers.value.isEmpty()) {
                    _errorMessage.value = null // Clear error to allow loading state to show
                    delay(1000) // Give network a second to stabilize
                    refresh()
                }
            }
        }
    }

    private fun observeNotifications() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.subscribeToNotifications(uid).collect {
                _notifications.value = it
            }
        }
    }

    fun markAsRead(notification: AppNotification) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.markNotificationRead(uid, notification)
        }
    }

    fun markAllAsRead() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.markAllNotificationsRead(uid)
        }
    }

    fun refresh() {
        fetchCenters()
    }

    private fun fetchCenters() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCenters()
                .onSuccess {
                    _centers.value = it
                    _errorMessage.value = null
                }
                .onFailure { exception ->
                    _errorMessage.value = ErrorMapper.getUserFriendlyMessage(exception)
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
