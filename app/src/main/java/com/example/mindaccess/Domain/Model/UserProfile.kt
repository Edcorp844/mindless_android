package com.example.mindaccess.Domain.Model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val photoURL: String? = null,
    val provider: String = "password",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val role: String = "user"
)
