package com.example.mindaccess.di

import com.example.mindaccess.Data.Remote.MindAccessApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMindAccessApi(): MindAccessApi {
        return Retrofit.Builder()
            .baseUrl(MindAccessApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MindAccessApi::class.java)
    }
}
