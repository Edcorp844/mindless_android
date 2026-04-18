package com.example.mindaccess.Data.Repository

import com.example.mindaccess.Data.Remote.MindAccessApi
import com.example.mindaccess.Domain.Model.*
import com.example.mindaccess.Domain.Repository.LegalRepository
import javax.inject.Inject

class LegalRepositoryImpl @Inject constructor(
    private val api: MindAccessApi
) : LegalRepository {

    override suspend fun getAndroidLicenses(): Result<LicenseResponse> {
        return try {
            Result.success(api.getAndroidLicenses())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTerms(): Result<TermsResponse> {
        return try {
            Result.success(api.getTerms())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHelp(): Result<HelpResponse> {
        return try {
            Result.success(api.getHelp())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFaq(): Result<FaqResponse> {
        return try {
            Result.success(api.getFaq())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
