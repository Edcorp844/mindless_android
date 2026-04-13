package com.example.mindaccess.di

import com.example.mindaccess.Data.Repository.CenterRepositoryImpl
import com.example.mindaccess.Domain.Repository.CenterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCenterRepository(
        centerRepositoryImpl: CenterRepositoryImpl
    ): CenterRepository
}
