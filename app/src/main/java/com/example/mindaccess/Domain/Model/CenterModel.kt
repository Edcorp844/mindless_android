package com.example.mindaccess.Domain.Model

import com.google.gson.annotations.SerializedName

data class CenterModel(
    val id: Int,
    val name: String,
    val description: String?,
    val category: CenterCategory,
    @SerializedName("cordinates")
    val coordinates: Coordinates,
    val open: String?,
    val services: List<String>,
    val contact: ContactInfo?
)

// MARK: - Contact Sub-models
data class ContactInfo(
    val phone: List<String>?,
    val email: List<ContactEmail>?,
    val other: List<ContactOther>?
)

data class ContactEmail(
    val type: String,
    val email: String
)

data class ContactOther(
    val type: String,
    val value: String
)

// MARK: - Metadata Models
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class CenterCategory(
    val id: Int,
    val label: String,
    val color: String,
    val icon: CategoryIcon?
)

data class CategoryIcon(
    val displayName: String?
)
