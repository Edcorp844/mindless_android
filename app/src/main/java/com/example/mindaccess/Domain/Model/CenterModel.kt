package com.example.mindaccess.Domain.Model

import com.google.gson.annotations.SerializedName

data class CenterModel(
    val id: Int,
    val name: String,
    val description: String?,
    val category: CenterCategoryModel,
    @SerializedName("cordinates")
    val location: GeoLocationCordModel,
    val open: String?,
    val contact: String?,
    val services: List<String> = emptyList(),
)

data class CenterCategoryModel(
    val id: Int,
    val label: String,
    val color: String,
    val icon: CategoryIconModel
)

data class CategoryIconModel(
    val displayName: String
)

data class GeoLocationCordModel(
    val latitude: Double,
    val longitude: Double
)
