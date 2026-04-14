package com.example.mindaccess.Domain.Repository

import com.example.mindaccess.Domain.Model.CenterModel

interface CenterRepository {
    suspend fun getCenters(): Result<List<CenterModel>>
}
