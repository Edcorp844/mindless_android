package com.example.mindaccess.Data.Remote

import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Model.FaqResponse
import com.example.mindaccess.Domain.Model.HelpResponse
import com.example.mindaccess.Domain.Model.LicenseResponse
import com.example.mindaccess.Domain.Model.TermsResponse
import retrofit2.http.GET

interface MindAccessApi {
    @GET("centers")
    suspend fun getCenters(): List<CenterModel>

    @GET("licenses/android")
    suspend fun getAndroidLicenses(): LicenseResponse

    @GET("terms")
    suspend fun getTerms(): TermsResponse

    @GET("help")
    suspend fun getHelp(): HelpResponse

    @GET("faq")
    suspend fun getFaq(): FaqResponse

    companion object {
        const val BASE_URL = "https://mindaccess.vercel.app/api/"
    }
}
