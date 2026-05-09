package com.example.mindaccess.ui.Screen.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindaccess.Domain.Repository.UserRepository
import com.example.mindaccess.utils.ErrorMapper
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _userState = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val userState = _userState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun setError(message: String) {
        _error.value = message
    }

    private fun handleAuthError(exception: Exception?) {
        _isLoading.value = false
        if (exception == null) {
            _error.value = "An unexpected error occurred."
            return
        }
        val message = exception.message ?: ""
        _error.value = when {
            exception is com.google.firebase.FirebaseNetworkException -> 
                "No internet connection. Please check your connection."
            message.contains("network", ignoreCase = true) -> 
                "No internet connection. Please check your connection."
            message.contains("user-not-found", ignoreCase = true) || 
            message.contains("wrong-password", ignoreCase = true) ||
            message.contains("invalid-credential", ignoreCase = true) ->
                "Invalid email or password."
            message.contains("email-already-in-use", ignoreCase = true) ->
                "This email is already registered."
            message.contains("too-many-requests", ignoreCase = true) ->
                "Too many attempts. Please try again later."
            else -> ErrorMapper.getUserFriendlyMessage(exception)
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            if (user != null) {
                                viewModelScope.launch {
                                    userRepository.upsertUserProfile(user)
                                    _userState.value = user
                                    _isLoading.value = false
                                    onSuccess()
                                }
                            } else {
                                _isLoading.value = false
                                onSuccess() // Should not happen
                            }
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName)
                                .build()

                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        val updatedUser = firebaseAuth.currentUser
                                        if (updatedUser != null) {
                                            viewModelScope.launch {
                                                userRepository.upsertUserProfile(updatedUser)
                                                _userState.value = updatedUser
                                                _isLoading.value = false
                                                onSuccess()
                                            }
                                        } else {
                                            _isLoading.value = false
                                            onSuccess()
                                        }
                                    } else {
                                        handleAuthError(updateTask.exception)
                                    }
                                }
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isLoading.value = false
                            onSuccess()
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun signInWithCredential(credential: AuthCredential, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            if (user != null) {
                                viewModelScope.launch {
                                    userRepository.upsertUserProfile(user)
                                    _userState.value = user
                                    _isLoading.value = false
                                    onSuccess()
                                }
                            } else {
                                _isLoading.value = false
                                onSuccess() // Should not happen
                            }
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun signInWithOAuth(activity: android.app.Activity, providerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val provider = OAuthProvider.newBuilder(providerId)
            
            firebaseAuth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            viewModelScope.launch {
                                userRepository.upsertUserProfile(user)
                                _userState.value = user
                                _isLoading.value = false
                                onSuccess()
                            }
                        } else {
                            _isLoading.value = false
                            onSuccess()
                        }
                    } else {
                        handleAuthError(task.exception)
                    }
                }
        }
    }

    fun signInAnonymously(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                firebaseAuth.signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            if (user != null) {
                                viewModelScope.launch {
                                    userRepository.upsertUserProfile(user)
                                    _userState.value = user
                                    _isLoading.value = false
                                    onSuccess()
                                }
                            } else {
                                _isLoading.value = false
                                onSuccess()
                            }
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _userState.value = null
    }
}
