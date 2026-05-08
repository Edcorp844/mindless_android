package com.example.mindaccess.Domain.Model

import com.google.firebase.Timestamp

sealed class AppNotification {
    abstract val id: String
    abstract val title: String
    abstract val body: String
    abstract val icon: String
    abstract val createdAt: Timestamp?
    abstract val read: Boolean

    data class Global(
        override val id: String = "",
        override val title: String = "",
        override val body: String = "",
        override val icon: String = "",
        override val createdAt: Timestamp? = null,
        override val read: Boolean = false,
        val type: String = "general"
    ) : AppNotification()

    data class Personal(
        override val id: String = "",
        override val title: String = "",
        override val body: String = "",
        override val icon: String = "",
        override val createdAt: Timestamp? = null,
        override val read: Boolean = false,
        val type: String = "account" // "account" or "system"
    ) : AppNotification()
}
