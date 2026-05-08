package com.example.mindaccess.Domain.Repository

import com.example.mindaccess.Domain.Model.AppNotification
import com.example.mindaccess.Domain.Model.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun upsertUserProfile(user: FirebaseUser)
    suspend fun getUserProfile(uid: String): UserProfile?
    suspend fun updateUserProfile(uid: String, displayName: String?, photoUrl: String?): Result<Unit>
    fun subscribeToNotifications(uid: String): Flow<List<AppNotification>>
    suspend fun markNotificationRead(uid: String, notification: AppNotification)
    suspend fun markAllNotificationsRead(uid: String)
}
