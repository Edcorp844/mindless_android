package com.example.mindaccess.Domain.Model

import com.google.gson.annotations.SerializedName

data class TermsResponse(
    val title: String,
    val version: String,
    val lastUpdated: String,
    val content: String
)

data class LicenseResponse(
    val platform: String,
    val lastUpdated: String,
    @SerializedName("libraries")
    val licenses: List<LicenseItem>
)

data class LicenseItem(
    val name: String,
    val licenseType: String,
    val url: String? = null,
    val content: String
)

data class HelpResponse(
    val title: String,
    val contact: HelpContact?,
    val faq: String?,
    @SerializedName("content")
    val description: String
)

data class HelpContact(
    val email: String?,
    val phone: String?
)

data class FaqResponse(
    val title: String,
    val lastUpdated: String,
    @SerializedName("questions")
    val items: List<FaqItem>
)

data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String
)
