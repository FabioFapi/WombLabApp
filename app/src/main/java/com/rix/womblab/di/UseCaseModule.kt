package com.rix.womblab.di

import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.domain.usecase.auth.*
import com.rix.womblab.domain.usecase.events.*
import com.rix.womblab.domain.usecase.favorites.*
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

    @Provides
    @Singleton
    fun provideGetEventsUseCase(
        eventRepository: EventRepository
    ): GetEventsUseCase = GetEventsUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideGetUpcomingEventsUseCase(
        eventRepository: EventRepository
    ): GetUpcomingEventsUseCase = GetUpcomingEventsUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideGetFeaturedEventsUseCase(
        eventRepository: EventRepository
    ): GetFeaturedEventsUseCase = GetFeaturedEventsUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideGetEventDetailUseCase(
        eventRepository: EventRepository
    ): GetEventDetailUseCase = GetEventDetailUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideSearchEventsUseCase(
        eventRepository: EventRepository
    ): SearchEventsUseCase = SearchEventsUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideRefreshEventsUseCase(
        eventRepository: EventRepository
    ): RefreshEventsUseCase = RefreshEventsUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideGetFavoritesUseCase(
        eventRepository: EventRepository
    ): GetFavoritesUseCase = GetFavoritesUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideAddToFavoritesUseCase(
        eventRepository: EventRepository
    ): AddToFavoritesUseCase = AddToFavoritesUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideRemoveFromFavoritesUseCase(
        eventRepository: EventRepository
    ): RemoveFromFavoritesUseCase = RemoveFromFavoritesUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideIsFavoriteUseCase(
        eventRepository: EventRepository
    ): IsFavoriteUseCase = IsFavoriteUseCase(eventRepository)

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(
        eventRepository: EventRepository
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(eventRepository)
}