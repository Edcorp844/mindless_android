package com.example.mindaccess.Data.Repository

import com.example.mindaccess.Data.Remote.MindAccessApi
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Repository.CenterRepository
import javax.inject.Inject

class CenterRepositoryImpl @Inject constructor(
    private val api: MindAccessApi
) : CenterRepository {
    override suspend fun getCenters(): Result<List<CenterModel>> {
        return try {
            val centers = api.getCenters()
            Result.success(centers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
