package com.example.mindaccess.Data.Repository

import com.example.mindaccess.Domain.Model.AppNotification
import com.example.mindaccess.Domain.Model.UserProfile
import com.example.mindaccess.Domain.Repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun upsertUserProfile(user: FirebaseUser) {
        val userRef = firestore.collection("users").document(user.uid)
        val snapshot = userRef.get().await()

        val provider = user.providerData.getOrNull(0)?.providerId ?: "password"

        if (!snapshot.exists()) {
            val profile = mapOf(
                "uid" to user.uid,
                "displayName" to user.displayName,
                "email" to user.email,
                "photoURL" to user.photoUrl?.toString(),
                "provider" to provider,
                "role" to "user",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(profile).await()

            // Welcome notification
            addPersonalNotification(
                user.uid,
                title = "Welcome to MindAccess! 🎉",
                body = "Your account has been created successfully. Explore the directory to find mental health and rehabilitation centers near you.",
                type = "account",
                icon = "🎉"
            )
        } else {
            userRef.update(
                mapOf(
                    "displayName" to user.displayName,
                    "photoURL" to user.photoUrl?.toString(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    override suspend fun getUserProfile(uid: String): UserProfile? {
        return firestore.collection("users").document(uid).get().await()
            .toObject(UserProfile::class.java)
    }

    private suspend fun addPersonalNotification(
        uid: String,
        title: String,
        body: String,
        type: String,
        icon: String
    ) {
        val notification = mapOf(
            "title" to title,
            "body" to body,
            "type" to type,
            "icon" to icon,
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(uid)
            .collection("notifications").add(notification).await()
    }

    override fun subscribeToNotifications(uid: String): Flow<List<AppNotification>> {
        val globalFlow = callbackFlow<List<AppNotification.Global>> {
            val listener = firestore.collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(AppNotification.Global::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    trySend(notifications)
                }
            awaitClose { listener.remove() }
        }

        val readReceiptsFlow = callbackFlow<Set<String>> {
            val listener = firestore.collection("users").document(uid)
                .collection("readGlobal")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val ids = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                    trySend(ids)
                }
            awaitClose { listener.remove() }
        }

        val personalFlow = callbackFlow<List<AppNotification.Personal>> {
            val listener = firestore.collection("users").document(uid)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(AppNotification.Personal::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    trySend(notifications)
                }
            awaitClose { listener.remove() }
        }

        return combine(globalFlow, readReceiptsFlow, personalFlow) { globals, readIds, personals ->
            val updatedGlobals = globals.map { it.copy(read = readIds.contains(it.id)) }
            (updatedGlobals + personals).sortedByDescending { it.createdAt?.seconds ?: 0 }
        }
    }

    override suspend fun markNotificationRead(uid: String, notification: AppNotification) {
        when (notification) {
            is AppNotification.Global -> {
                firestore.collection("users").document(uid)
                    .collection("readGlobal").document(notification.id)
                    .set(mapOf("readAt" to FieldValue.serverTimestamp())).await()
            }
            is AppNotification.Personal -> {
                firestore.collection("users").document(uid)
                    .collection("notifications").document(notification.id)
                    .update("read", true).await()
            }
        }
    }

    override suspend fun markAllNotificationsRead(uid: String) {
        val batch = firestore.batch()

        // Mark all global read (by creating receipts)
        val globals = firestore.collection("notifications").get().await()
        globals.documents.forEach { doc ->
            val ref = firestore.collection("users").document(uid)
                .collection("readGlobal").document(doc.id)
            batch.set(ref, mapOf("readAt" to FieldValue.serverTimestamp()))
        }

        // Mark all personal read
        val personals = firestore.collection("users").document(uid)
            .collection("notifications").whereEqualTo("read", false).get().await()
        personals.documents.forEach { doc ->
            batch.update(doc.reference, "read", true)
        }

        batch.commit().await()
    }
}
