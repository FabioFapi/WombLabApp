package com.rix.womblab.di

import com.rix.womblab.data.repository.AuthRepositoryImpl
import com.rix.womblab.data.repository.EventRepositoryImpl
import com.rix.womblab.data.repository.NotificationRepositoryImpl
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.domain.repository.NotificationRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}