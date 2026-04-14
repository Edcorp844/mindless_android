package com.example.mindaccess.Data.Remote

import com.example.mindaccess.Domain.Model.CenterModel
import retrofit2.http.GET

interface MindAccessApi {
    @GET("centers")
    suspend fun getCenters(): List<CenterModel>

    companion object {
        const val BASE_URL = "https://mindaccess.vercel.app/api/"
    }
}
