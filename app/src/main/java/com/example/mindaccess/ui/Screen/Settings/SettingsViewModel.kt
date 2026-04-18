package com.example.mindaccess.ui.Screen.Settings

import android.content.Context
import android.net.TrafficStats
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Model.*
import com.example.mindaccess.Domain.Repository.LegalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val legalRepository: LegalRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

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

    init {
        updateDataUsage()
        fetchLegalData()
        // Periodically update usage
        viewModelScope.launch {
            while (true) {
                delay(5000)
                updateDataUsage()
            }
        }
    }

    private fun fetchLegalData() {
        viewModelScope.launch {
            _termsState.value = LegalState.Loading
            legalRepository.getTerms()
                .onSuccess { _termsState.value = LegalState.Success(it) }
                .onFailure { _termsState.value = LegalState.Error(it.message ?: "Unknown error") }

            _licenseState.value = LegalState.Loading
            legalRepository.getAndroidLicenses()
                .onSuccess { _licenseState.value = LegalState.Success(it) }
                .onFailure { _licenseState.value = LegalState.Error(it.message ?: "Unknown error") }

            _helpState.value = LegalState.Loading
            legalRepository.getHelp()
                .onSuccess { _helpState.value = LegalState.Success(it) }
                .onFailure { _helpState.value = LegalState.Error(it.message ?: "Unknown error") }

            _faqState.value = LegalState.Loading
            legalRepository.getFaq()
                .onSuccess { _faqState.value = LegalState.Success(it) }
                .onFailure { _faqState.value = LegalState.Error(it.message ?: "Unknown error") }
        }
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
