package com.example.mindaccess.Domain.Model

import java.util.Hashtable

data class CenterModel(
    val name: String,
    val description: String?,
    val location: GeoLocationCordModel,
    val category: String,
    val workingDays: String?,
    val contact: Hashtable<String, String>?,
    val services: List<String>?
)
