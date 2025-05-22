package com.rix.womblab.di

import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.auth.LoginUseCase
import com.rix.womblab.domain.usecase.auth.LogoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase = LoginUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(
        authRepository: AuthRepository
    ): GetCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
}