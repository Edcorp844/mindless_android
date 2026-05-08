package com.example.mindaccess.di

import com.example.mindaccess.Data.Repository.CenterRepositoryImpl
import com.example.mindaccess.Data.Repository.LegalRepositoryImpl
import com.example.mindaccess.Data.Repository.UserRepositoryImpl
import com.example.mindaccess.Domain.Repository.CenterRepository
import com.example.mindaccess.Domain.Repository.LegalRepository
import com.example.mindaccess.Domain.Repository.UserRepository
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

    @Binds
    @Singleton
    abstract fun bindLegalRepository(
        legalRepositoryImpl: LegalRepositoryImpl
    ): LegalRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
