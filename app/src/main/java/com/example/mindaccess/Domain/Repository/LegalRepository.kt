package com.example.mindaccess.Domain.Repository

import com.example.mindaccess.Domain.Model.*

interface LegalRepository {
    suspend fun getAndroidLicenses(): Result<LicenseResponse>
    suspend fun getTerms(): Result<TermsResponse>
    suspend fun getHelp(): Result<HelpResponse>
    suspend fun getFaq(): Result<FaqResponse>
}
