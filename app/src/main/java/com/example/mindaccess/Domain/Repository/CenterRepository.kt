package com.example.mindaccess.Domain.Repository

import com.example.mindaccess.Domain.Model.CenterModel
import kotlinx.coroutines.flow.Flow

interface CenterRepository {
    fun getCenters(): Flow<List<CenterModel>>
}
