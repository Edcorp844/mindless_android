package com.example.mindaccess.ui.Screen.Settings

import android.content.Context
import android.net.TrafficStats
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Model.*
import com.example.mindaccess.Domain.Repository.LegalRepository
import com.example.mindaccess.utils.ErrorMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

sealed class LegalState<out T> {
    object Loading : LegalState<Nothing>()
    data class Success<T>(val data: T) : LegalState<T>()
    data class Error(val message: String) : LegalState<Nothing>()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val legalRepository: LegalRepository,
    private val userRepository: com.example.mindaccess.Domain.Repository.UserRepository
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    // Properties initialized before init block
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)
    val userState: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _locationEnabled = MutableStateFlow(sharedPrefs.getBoolean("location_enabled", true))
    val locationEnabled = _locationEnabled.asStateFlow()

    private val _dataUsage = MutableStateFlow("0.0 MB")
    val dataUsage = _dataUsage.asStateFlow()

    private val _termsState = MutableStateFlow<LegalState<TermsResponse>>(LegalState.Loading)
    val termsState = _termsState.asStateFlow()

    private val _licenseState = MutableStateFlow<LegalState<LicenseResponse>>(LegalState.Loading)
    val licenseState = _licenseState.asStateFlow()

    private val _helpState = MutableStateFlow<LegalState<HelpResponse>>(LegalState.Loading)
    val helpState = _helpState.asStateFlow()

    private val _faqState = MutableStateFlow<LegalState<FaqResponse>>(LegalState.Loading)
    val faqState = _faqState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    init {
        // Ensure all properties are initialized before starting observers
        observeNotifications()
        observeUserProfile()
        updateDataUsage()
        fetchLegalData()

        viewModelScope.launch {
            while (true) {
                delay(5000)
                updateDataUsage()
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            _currentUser.collect { user ->
                if (user != null) {
                    _userProfile.value = userRepository.getUserProfile(user.uid)
                } else {
                    _userProfile.value = null
                }
            }
        }
    }

    fun updateProfile(displayName: String, photoUrl: String?, onResult: (Boolean) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.updateUserProfile(uid, displayName, photoUrl)
                .onSuccess {
                    _userProfile.value = _userProfile.value?.copy(
                        displayName = displayName,
                        photoURL = photoUrl
                    )
                    onResult(true)
                }
                .onFailure { onResult(false) }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            // Using _currentUser directly as an extra safety measure against init order issues
            _currentUser.collect { user ->
                val uid = user?.uid
                if (uid != null) {
                    userRepository.subscribeToNotifications(uid).collect {
                        _notifications.value = it
                    }
                } else {
                    _notifications.value = emptyList()
                }
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

    private fun fetchLegalData() {
        viewModelScope.launch {
            _termsState.value = LegalState.Loading
            legalRepository.getTerms()
                .onSuccess { _termsState.value = LegalState.Success(it) }
                .onFailure { _termsState.value = LegalState.Error(ErrorMapper.getUserFriendlyMessage(it)) }

            _licenseState.value = LegalState.Loading
            legalRepository.getAndroidLicenses()
                .onSuccess { _licenseState.value = LegalState.Success(it) }
                .onFailure { _licenseState.value = LegalState.Error(ErrorMapper.getUserFriendlyMessage(it)) }

            _helpState.value = LegalState.Loading
            legalRepository.getHelp()
                .onSuccess { _helpState.value = LegalState.Success(it) }
                .onFailure { _helpState.value = LegalState.Error(ErrorMapper.getUserFriendlyMessage(it)) }

            _faqState.value = LegalState.Loading
            legalRepository.getFaq()
                .onSuccess { _faqState.value = LegalState.Success(it) }
                .onFailure { _faqState.value = LegalState.Error(ErrorMapper.getUserFriendlyMessage(it)) }
        }
    }

    fun updateCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun signInWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateCurrentUser()
                            onResult(true, null)
                        } else {
                            onResult(false, task.exception?.message ?: "Authentication failed")
                        }
                    }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateCurrentUser()
                            onResult(true, null)
                        } else {
                            onResult(false, task.exception?.message ?: "Account creation failed")
                        }
                    }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }


    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    fun setLocationEnabled(enabled: Boolean) {
        _locationEnabled.value = enabled
        sharedPrefs.edit().putBoolean("location_enabled", enabled).apply()
    }

    private fun updateDataUsage() {
        val uid = Process.myUid()
        val received = TrafficStats.getUidRxBytes(uid)
        val sent = TrafficStats.getUidTxBytes(uid)
        
        val totalBytes = received + sent
        if (totalBytes <= 0) {
            _dataUsage.value = "0.0 MB"
            return
        }

        val mb = totalBytes / (1024.0 * 1024.0)
        _dataUsage.value = String.format(Locale.getDefault(), "%.2f MB", mb)
    }
}
